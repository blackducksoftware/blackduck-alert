/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.validators.BlackDuckValidator;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckAccumulator extends ProviderTask {
    public static final String TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE = "last.search.end.date";

    private final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);

    private final BlackDuckProviderKey blackDuckProviderKey;
    private final NotificationManager notificationManager;
    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private final BlackDuckValidator blackDuckValidator;

    public BlackDuckAccumulator(BlackDuckProviderKey blackDuckProviderKey, TaskScheduler taskScheduler, NotificationManager notificationManager, ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor,
        ProviderProperties providerProperties, BlackDuckValidator blackDuckValidator) {
        super(blackDuckProviderKey, taskScheduler, providerProperties);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.notificationManager = notificationManager;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
        this.blackDuckValidator = blackDuckValidator;
    }

    public String formatDate(OffsetDateTime date) {
        return DateUtils.formatDate(date, RestConstants.JSON_DATE_FORMAT);
    }

    @Override
    public void runProviderTask() {
        if (blackDuckValidator.validate(getProviderProperties())) {
            accumulate();
        }
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    public void accumulate() {
        try {
            DateRange dateRange = createDateRange();
            OffsetDateTime nextSearchStartTime = accumulate(dateRange);
            String nextSearchStartString = formatDate(nextSearchStartTime);
            logger.info("Accumulator Next Range Start Time: {} ", nextSearchStartString);
            saveNextSearchStart(nextSearchStartString);
        } catch (AlertDatabaseConstraintException e) {
            logger.error("Error occurred accumulating data! ", e);
        }
    }

    protected Optional<String> getNextSearchStart() {
        return providerTaskPropertiesAccessor.getTaskProperty(getTaskName(), BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE);
    }

    protected void saveNextSearchStart(String nextSearchStart) throws AlertDatabaseConstraintException {
        providerTaskPropertiesAccessor.setTaskProperty(getProviderProperties().getConfigId(), getTaskName(), BlackDuckAccumulator.TASK_PROPERTY_KEY_LAST_SEARCH_END_DATE, nextSearchStart);
    }

    protected DateRange createDateRange() {
        OffsetDateTime endDate = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startDate = endDate;
        try {
            Optional<String> nextSearchStartTime = getNextSearchStart();
            if (nextSearchStartTime.isPresent()) {
                String lastRunValue = nextSearchStartTime.get();
                startDate = parseDateString(lastRunValue);
            } else {
                startDate = endDate.minusMinutes(1);
            }
        } catch (ParseException e) {
            logger.error("Error creating date range", e);
        }
        return DateRange.of(startDate, endDate);
    }

    protected OffsetDateTime parseDateString(String date) throws ParseException {
        return DateUtils.parseDate(date, RestConstants.JSON_DATE_FORMAT);
    }

    protected OffsetDateTime accumulate(DateRange dateRange) {
        OffsetDateTime currentStartTime = dateRange.getStart();
        Optional<OffsetDateTime> latestNotificationCreatedAtDate = Optional.empty();

        List<NotificationView> notifications = read(dateRange);
        if (!notifications.isEmpty()) {
            List<NotificationView> sortedNotifications = sort(notifications);
            List<AlertNotificationModel> contentList = process(sortedNotifications);
            write(contentList);
            latestNotificationCreatedAtDate = getLatestNotificationCreatedAtDate(sortedNotifications);
        }
        return calculateNextStartTime(latestNotificationCreatedAtDate, currentStartTime);
    }

    protected List<NotificationView> read(DateRange dateRange) {
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = getProviderProperties().createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            try {
                BlackDuckServicesFactory blackDuckServicesFactory = getProviderProperties().createBlackDuckServicesFactory(optionalBlackDuckHttpClient.get(), new Slf4jIntLogger(logger));
                OffsetDateTime startDate = dateRange.getStart();
                OffsetDateTime endDate = dateRange.getEnd();
                logger.info("Accumulating Notifications Between {} and {} ", DateUtils.formatDate(startDate, RestConstants.JSON_DATE_FORMAT), DateUtils.formatDate(endDate, RestConstants.JSON_DATE_FORMAT));

                NotificationService notificationService = blackDuckServicesFactory.createNotificationService();
                List<NotificationView> notificationViews = notificationService.getFilteredNotifications(Date.from(startDate.toInstant()), Date.from(endDate.toInstant()), getNotificationTypes());
                logger.debug("Read Notification Count: {}", notificationViews.size());
                return notificationViews;
            } catch (Exception ex) {
                logger.error("Error Reading notifications", ex);
            }
        }
        return List.of();
    }

    private List<String> getNotificationTypes() {
        return Stream.of(NotificationType.values())
                   .filter(type -> type != NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED)
                   .map(Enum::name).collect(Collectors.toList());
    }

    protected List<AlertNotificationModel> process(List<NotificationView> notifications) {
        logger.info("Processing accumulated notifications");
        return notifications
                   .stream()
                   .map(this::createContent)
                   .collect(Collectors.toList());
    }

    protected void write(List<AlertNotificationModel> contentList) {
        logger.info("Writing Notifications...");
        notificationManager.saveAllNotifications(contentList);
    }

    private List<NotificationView> sort(List<NotificationView> notifications) {
        return notifications
                   .stream()
                   .sorted(Comparator.comparing(NotificationView::getCreatedAt))
                   .collect(Collectors.toList());
    }

    private AlertNotificationModel createContent(NotificationView notification) {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = DateUtils.fromDateUTC(notification.getCreatedAt());
        String provider = blackDuckProviderKey.getUniversalKey();
        String notificationType = notification.getType().name();
        String jsonContent = notification.getJson();
        return new AlertNotificationModel(null, getProviderProperties().getConfigId(), provider, null, notificationType, jsonContent, createdAt, providerCreationTime);
    }

    // Expects that the notifications are sorted oldest to newest
    private Optional<OffsetDateTime> getLatestNotificationCreatedAtDate(List<NotificationView> sortedNotificationList) {
        if (!sortedNotificationList.isEmpty()) {
            int lastIndex = sortedNotificationList.size() - 1;
            NotificationView notificationView = sortedNotificationList.get(lastIndex);
            OffsetDateTime createdAtDate = DateUtils.fromDateUTC(notificationView.getCreatedAt());
            return Optional.of(createdAtDate);
        }
        return Optional.empty();
    }

    private OffsetDateTime calculateNextStartTime(Optional<OffsetDateTime> latestNotificationCreatedAt, OffsetDateTime currentStartDate) {
        OffsetDateTime newStartDate = currentStartDate;
        if (latestNotificationCreatedAt.isPresent()) {
            OffsetDateTime latestNotification = latestNotificationCreatedAt.get();
            // increment 1 millisecond
            newStartDate = latestNotification.plusNanos(1000000);
            logger.info("Notifications found; updating to latest notification found");
        } else {
            logger.info("No notifications found; using current search time");
        }
        return newStartDate;
    }

}
