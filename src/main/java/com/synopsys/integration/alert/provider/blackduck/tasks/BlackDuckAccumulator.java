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

import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;

public class BlackDuckAccumulator extends ProviderTask {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);

    private final NotificationManager notificationManager;
    private final FilePersistenceUtil filePersistenceUtil;
    private BlackDuckProviderKey blackDuckProviderKey;

    public BlackDuckAccumulator(BlackDuckProviderKey blackDuckProviderKey, TaskScheduler taskScheduler, NotificationManager notificationManager, FilePersistenceUtil filePersistenceUtil, ProviderProperties providerProperties) {
        super(blackDuckProviderKey, taskScheduler, providerProperties);
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.notificationManager = notificationManager;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public String formatDate(Date date) {
        return RestConstants.formatDate(date);
    }

    public String getSearchRangeFileName() {
        // FIXME delete this
        return null;
    }

    @Override
    public void runProviderTask() {
        accumulate();
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    public void accumulate() {
        try {
            // FIXME read from the database
            //            if (!filePersistenceUtil.exists(getSearchRangeFileName())) {
            //                initializeSearchRangeFile();
            //            }
            DateRange dateRange = null; // FIXME createDateRange(getSearchRangeFileName());
            Date nextSearchStartTime = accumulate(dateRange);
            String nextSearchStartString = formatDate(nextSearchStartTime);
            logger.info("Accumulator Next Range Start Time: {} ", nextSearchStartString);
            saveNextSearchStart(nextSearchStartString);
        } catch (IOException ex) {
            logger.error("Error occurred accumulating data! ", ex);
        } finally {
            Optional<Long> nextRun = getMillisecondsToNextRun();
            if (nextRun.isPresent()) {
                Long seconds = TimeUnit.MILLISECONDS.toSeconds(nextRun.get());
                logger.debug("Accumulator next run: {} seconds", seconds);
            }
        }
    }

    protected void initializeSearchRangeFile() throws IOException {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withSecond(0).withNano(0);
        Date date = Date.from(zonedDate.toInstant());
        // FIXME replace this with database write: filePersistenceUtil.writeToFile(getSearchRangeFileName(), formatDate(date));
    }

    protected void saveNextSearchStart(String nextSearchStart) throws IOException {
        // FIXME replace this with database write: filePersistenceUtil.writeToFile(getSearchRangeFileName(), nextSearchStart);
    }

    protected DateRange createDateRange(String lastSearchFileName) {
        ZonedDateTime zonedEndDate = ZonedDateTime.now();
        zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
        ZonedDateTime zonedStartDate = zonedEndDate;
        Date endDate = Date.from(zonedEndDate.toInstant());

        Date startDate = Date.from(zonedStartDate.toInstant());
        try {
            if (filePersistenceUtil.exists(lastSearchFileName)) {
                String lastRunValue = readSearchStartTime(lastSearchFileName);
                Date startTime = parseDateString(lastRunValue);
                zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
            } else {
                zonedStartDate = zonedEndDate.minusMinutes(1);
            }
            startDate = Date.from(zonedStartDate.toInstant());
        } catch (IOException | ParseException e) {
            logger.error("Error creating date range", e);
        }
        return DateRange.of(startDate, endDate);
    }

    protected String readSearchStartTime(String lastSearchFileName) throws IOException {
        return filePersistenceUtil.readFromFile(lastSearchFileName);
    }

    protected Date parseDateString(String date) throws ParseException {
        return RestConstants.parseDateString(date);
    }

    protected Date accumulate(DateRange dateRange) {
        Date currentStartTime = dateRange.getStart();
        Optional<Date> latestNotificationCreatedAtDate = Optional.empty();

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
                Date startDate = dateRange.getStart();
                Date endDate = dateRange.getEnd();
                logger.info("Accumulating Notifications Between {} and {} ", RestConstants.formatDate(startDate), RestConstants.formatDate(endDate));

                NotificationService notificationService = blackDuckServicesFactory.createNotificationService();
                List<NotificationView> notificationViews = notificationService.getFilteredNotifications(startDate, endDate, getNotificationTypes());
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
        Date createdAt = Date.from(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toInstant());
        Date providerCreationTime = notification.getCreatedAt();
        String provider = blackDuckProviderKey.getUniversalKey();
        String notificationType = notification.getType().name();
        String jsonContent = notification.getJson();
        return new AlertNotificationModel(null, getProviderProperties().getConfigId(), provider, null, notificationType, jsonContent, createdAt, providerCreationTime);
    }

    // Expects that the notifications are sorted oldest to newest
    private Optional<Date> getLatestNotificationCreatedAtDate(List<NotificationView> sortedNotificationList) {
        if (!sortedNotificationList.isEmpty()) {
            int lastIndex = sortedNotificationList.size() - 1;
            NotificationView notificationView = sortedNotificationList.get(lastIndex);
            return Optional.of(notificationView.getCreatedAt());
        }
        return Optional.empty();
    }

    private Date calculateNextStartTime(Optional<Date> latestNotificationCreatedAt, Date currentStartDate) {
        Date newStartDate = currentStartDate;
        if (latestNotificationCreatedAt.isPresent()) {
            Date latestNotification = latestNotificationCreatedAt.get();
            ZonedDateTime newSearchStart = ZonedDateTime.ofInstant(latestNotification.toInstant(), ZoneOffset.UTC);
            // increment 1 millisecond
            newSearchStart = newSearchStart.plusNanos(1000000);
            newStartDate = Date.from(newSearchStart.toInstant());
            logger.info("Notifications found; updating to latest notification found");
        } else {
            logger.info("No notifications found; using current search time");
        }
        return newStartDate;
    }

}
