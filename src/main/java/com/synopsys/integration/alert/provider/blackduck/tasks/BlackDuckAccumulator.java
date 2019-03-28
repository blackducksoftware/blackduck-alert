/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.BlackDuckRequestFilter;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.request.Request;

@Component
public class BlackDuckAccumulator extends ScheduledTask {
    public static final String TASK_NAME = "blackduck-accumulator-task";
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);
    private final BlackDuckProperties blackDuckProperties;
    private final NotificationManager notificationManager;
    private final FilePersistenceUtil filePersistenceUtil;
    private final String searchRangeFileName;

    @Autowired
    public BlackDuckAccumulator(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final NotificationManager notificationManager, final FilePersistenceUtil filePersistenceUtil) {
        super(taskScheduler, TASK_NAME);
        this.blackDuckProperties = blackDuckProperties;
        this.notificationManager = notificationManager;
        this.filePersistenceUtil = filePersistenceUtil;
        searchRangeFileName = String.format("%s-last-search.txt", getTaskName());
    }

    public String getSearchRangeFileName() {
        return searchRangeFileName;
    }

    public String formatDate(final Date date) {
        return RestConstants.formatDate(date);
    }

    @Override
    public void runTask() {
        accumulate();
    }

    public void accumulate() {
        try {
            if (!filePersistenceUtil.exists(getSearchRangeFileName())) {
                initializeSearchRangeFile();
            }
            final DateRange dateRange = createDateRange(getSearchRangeFileName());
            final Date nextSearchStartTime = accumulate(dateRange);
            final String nextSearchStartString = formatDate(nextSearchStartTime);
            logger.info("Accumulator Next Range Start Time: {} ", nextSearchStartString);
            saveNextSearchStart(nextSearchStartString);
        } catch (final IOException ex) {
            logger.error("Error occurred accumulating data! ", ex);
        } finally {
            final Optional<Long> nextRun = getMillisecondsToNextRun();
            if (nextRun.isPresent()) {
                final Long seconds = TimeUnit.MILLISECONDS.toSeconds(nextRun.get());
                logger.debug("Accumulator next run: {} seconds", seconds);
            }
        }
    }

    protected void initializeSearchRangeFile() throws IOException {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        filePersistenceUtil.writeToFile(getSearchRangeFileName(), formatDate(date));
    }

    protected void saveNextSearchStart(final String nextSearchStart) throws IOException {
        filePersistenceUtil.writeToFile(getSearchRangeFileName(), nextSearchStart);
    }

    protected DateRange createDateRange(final String lastSearchFileName) {
        ZonedDateTime zonedEndDate = ZonedDateTime.now();
        zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
        ZonedDateTime zonedStartDate = zonedEndDate;
        final Date endDate = Date.from(zonedEndDate.toInstant());

        Date startDate = Date.from(zonedStartDate.toInstant());
        try {
            if (filePersistenceUtil.exists(lastSearchFileName)) {
                final String lastRunValue = readSearchStartTime(lastSearchFileName);
                final Date startTime = parseDateString(lastRunValue);
                zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
            } else {
                zonedStartDate = zonedEndDate.minusMinutes(1);
            }
            startDate = Date.from(zonedStartDate.toInstant());
        } catch (final IOException | ParseException e) {
            logger.error("Error creating date range", e);
        }
        return DateRange.of(startDate, endDate);
    }

    protected String readSearchStartTime(final String lastSearchFileName) throws IOException {
        return filePersistenceUtil.readFromFile(lastSearchFileName);
    }

    protected Date parseDateString(final String date) throws ParseException {
        return RestConstants.parseDateString(date);
    }

    protected Date accumulate(final DateRange dateRange) {
        final Date currentStartTime = dateRange.getStart();
        Optional<Date> latestNotificationCreatedAtDate = Optional.empty();

        final List<NotificationView> notifications = read(dateRange);
        if (!notifications.isEmpty()) {
            final List<NotificationView> sortedNotifications = sort(notifications);
            final List<NotificationContent> contentList = process(sortedNotifications);
            write(contentList);
            latestNotificationCreatedAtDate = getLatestNotificationCreatedAtDate(sortedNotifications);
        }
        return calculateNextStartTime(latestNotificationCreatedAtDate, currentStartTime);
    }

    protected List<NotificationView> read(final DateRange dateRange) {
        final Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            try {
                final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(optionalBlackDuckHttpClient.get(), new Slf4jIntLogger(logger));
                final Date startDate = dateRange.getStart();
                final Date endDate = dateRange.getEnd();
                logger.info("Accumulating Notifications Between {} and {} ", RestConstants.formatDate(startDate), RestConstants.formatDate(endDate));

                // There is a bug in NotificationService.getFilteredNotifications(...) blackduck-common 41.2.0
                // TODO remove the method getNotifications once the notification service is fixed
                final List<NotificationView> notificationViews = getNotifications(startDate, endDate, blackDuckServicesFactory.createBlackDuckService());
                logger.debug("Read Notification Count: {}", notificationViews.size());
                return notificationViews;
            } catch (final Exception ex) {
                logger.error("Error Reading notifications", ex);
            }
        }
        return List.of();
    }

    private List<String> getNotificationTypes() {
        return Stream.of(NotificationType.values())
                   .filter(type -> type != NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED && type != NotificationType.BOM_EDIT)
                   .map(Enum::name).collect(Collectors.toList());
    }

    private List<NotificationView> getNotifications(final Date startDate, final Date endDate, final BlackDuckService blackDuckService) throws IntegrationException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String startDateString = sdf.format(startDate);
        final String endDateString = sdf.format(endDate);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().addQueryParameter("startDate", startDateString).addQueryParameter("endDate", endDateString);
        final BlackDuckRequestFilter notificationTypeFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("notificationType", getNotificationTypes());
        RequestFactory.addBlackDuckFilter(requestBuilder, notificationTypeFilter);
        return blackDuckService.getResponses(ApiDiscovery.NOTIFICATIONS_LINK_RESPONSE, requestBuilder, true);
    }

    protected List<NotificationContent> process(final List<NotificationView> notifications) {
        logger.info("Processing accumulated notifications");
        return notifications
                   .stream()
                   .map(this::createContent)
                   .collect(Collectors.toList());
    }

    protected void write(final List<NotificationContent> contentList) {
        logger.info("Writing Notifications...");
        contentList.forEach(notificationManager::saveNotification);
    }

    private List<NotificationView> sort(final List<NotificationView> notifications) {
        return notifications
                   .stream()
                   .sorted(Comparator.comparing(NotificationView::getCreatedAt))
                   .collect(Collectors.toList());
    }

    private NotificationContent createContent(final NotificationView notification) {
        final Date createdAt = Date.from(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toInstant());
        final Date providerCreationTime = notification.getCreatedAt();
        final String provider = BlackDuckProvider.COMPONENT_NAME;
        final String notificationType = notification.getType().name();
        final String jsonContent = notification.getJson();
        return new NotificationContent(createdAt, provider, providerCreationTime, notificationType, jsonContent);
    }

    // Expects that the notifications are sorted oldest to newest
    private Optional<Date> getLatestNotificationCreatedAtDate(final List<NotificationView> sortedNotificationList) {
        if (!sortedNotificationList.isEmpty()) {
            final int lastIndex = sortedNotificationList.size() - 1;
            final NotificationView notificationView = sortedNotificationList.get(lastIndex);
            return Optional.of(notificationView.getCreatedAt());
        }
        return Optional.empty();
    }

    private Date calculateNextStartTime(final Optional<Date> latestNotificationCreatedAt, final Date currentStartDate) {
        Date newStartDate = currentStartDate;
        if (latestNotificationCreatedAt.isPresent()) {
            final Date latestNotification = latestNotificationCreatedAt.get();
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
