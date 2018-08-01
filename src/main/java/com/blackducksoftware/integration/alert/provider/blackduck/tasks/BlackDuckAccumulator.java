/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.provider.blackduck.tasks;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.scheduled.ScheduledTask;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationViewResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

@Component
public class BlackDuckAccumulator extends ScheduledTask {
    public static final String DEFAULT_CRON_EXPRESSION = "0 0/1 * 1/1 * *";
    public static final String ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulator.class);

    private final BlackDuckProperties blackDuckProperties;
    private final List<NotificationTypeProcessor> notificationProcessors;
    private final ContentConverter contentConverter;
    private final NotificationManager notificationManager;
    private final File searchRangeFilePath;

    @Autowired
    public BlackDuckAccumulator(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final ContentConverter contentConverter,
            final NotificationManager notificationManager, final List<NotificationTypeProcessor> notificationProcessors) {
        super(taskScheduler, "blackduck-accumulator-task");
        this.blackDuckProperties = blackDuckProperties;
        this.notificationProcessors = notificationProcessors;
        this.contentConverter = contentConverter;
        this.notificationManager = notificationManager;
        //TODO: do not store a file with the timestamp save this information into a database table for tasks.  Perhaps a task metadata object stored in the database.
        final String accumulatorFileName = String.format("%s-last-search.txt", getTaskName());
        this.searchRangeFilePath = new File(blackDuckProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME), accumulatorFileName);
    }

    public File getSearchRangeFilePath() {
        return searchRangeFilePath;
    }

    public String formatDate(final Date date) {
        return RestConnection.formatDate(date);
    }

    public String createLoggerMessage(final String messageFormat) {
        return String.format("[ %s ] %s", getTaskName(), messageFormat);
    }

    @Override
    public void run() {
        accumulate();
    }

    public void accumulate() {
        logger.info(createLoggerMessage("### Accumulator Starting Operation..."));
        try {
            if (!getSearchRangeFilePath().exists()) {
                initializeSearchRangeFile();
            }
            final DateRange dateRange = createDateRange(getSearchRangeFilePath());
            final Date nextSearchStartTime = accumulate(dateRange);
            final String nextSearchStartString = formatDate(nextSearchStartTime);
            logger.info(createLoggerMessage("Accumulator Next Range Start Time: {} "), nextSearchStartString);
            saveNextSearchStart(nextSearchStartString);
        } catch (final IOException ex) {
            logger.error(createLoggerMessage("Error occurred accumulating data! "), ex);
        } finally {
            final Optional<Long> nextRun = getMillisecondsToNextRun();
            if (nextRun.isPresent()) {
                final Long seconds = TimeUnit.MILLISECONDS.toSeconds(nextRun.get());
                logger.debug(createLoggerMessage("Accumulator next run: {} seconds"), seconds);
            }
            logger.info(createLoggerMessage("### Accumulator Finished Operation."));
        }
    }

    protected void initializeSearchRangeFile() throws IOException {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        FileUtils.write(getSearchRangeFilePath(), formatDate(date), ENCODING);
    }

    protected void saveNextSearchStart(final String nextSearchStart) throws IOException {
        FileUtils.write(getSearchRangeFilePath(), nextSearchStart, ENCODING);
    }

    protected DateRange createDateRange(final File lastSearchFile) {
        ZonedDateTime zonedEndDate = ZonedDateTime.now();
        zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
        ZonedDateTime zonedStartDate = zonedEndDate;
        final Date endDate = Date.from(zonedEndDate.toInstant());

        Date startDate = Date.from(zonedStartDate.toInstant());
        try {
            if (lastSearchFile.exists()) {
                final String lastRunValue = readSearchStartTime(lastSearchFile);
                final Date startTime = parseDateString(lastRunValue);
                zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
            } else {
                zonedStartDate = zonedEndDate.minusMinutes(1);
            }
            startDate = Date.from(zonedStartDate.toInstant());
        } catch (final IOException | ParseException e) {
            logger.error(createLoggerMessage("Error creating date range"), e);
        }
        return new DateRange(startDate, endDate);
    }

    protected String readSearchStartTime(final File lastSearchFile) throws IOException {
        return FileUtils.readFileToString(lastSearchFile, ENCODING);
    }

    protected Date parseDateString(final String date) throws ParseException {
        return RestConnection.parseDateString(date);
    }

    protected Date accumulate(final DateRange dateRange) {
        final Date currentStartTime = dateRange.getStart();
        Optional<Date> latestNotificationCreatedAtDate = Optional.empty();

        final Optional<CommonNotificationViewResults> results = read(dateRange);
        if (results.isPresent()) {
            final List<NotificationContent> contentList = process(results.get());
            write(contentList);
            latestNotificationCreatedAtDate = results.get().getLatestNotificationCreatedAtDate();
        }
        return calculateNextStartTime(latestNotificationCreatedAtDate, currentStartTime);
    }

    protected Optional<CommonNotificationViewResults> read(final DateRange dateRange) {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        final Optional<RestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final RestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection);
                    final Date startDate = dateRange.getStart();
                    final Date endDate = dateRange.getEnd();
                    logger.info(createLoggerMessage("Accumulating Notifications Between {} and {} "), RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
                    final HubBucket hubBucket = new HubBucket();
                    final NotificationService notificationService = hubServicesFactory.createNotificationService(true);
                    final CommonNotificationViewResults notificationResults = notificationService.getAllCommonNotificationViewResults(startDate, endDate);

                    if (notificationResults.isEmpty()) {
                        logger.debug(createLoggerMessage("Read Notification Count: 0"));
                        return Optional.empty();
                    }
                    logger.debug(createLoggerMessage("Read Notification Count: {}"), notificationResults.getResults().size());
                    return Optional.of(notificationResults);
                }
            } catch (final Exception ex) {
                logger.error(createLoggerMessage("Error Reading notifications"), ex);
            } finally {
                executor.shutdownNow();
            }
        }
        return Optional.empty();
    }

    protected List<NotificationContent> process(final CommonNotificationViewResults notificationData) {
        logger.info(createLoggerMessage("Processing accumulated notifications"));
        return notificationData.getResults().stream().map(this::createContent).collect(Collectors.toList());
    }

    protected NotificationContent createContent(final CommonNotificationView commonNotificationView) {

        final Date createdAt = Date.from(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toInstant());
        final String provider = "";
        final String notificationType = commonNotificationView.getType().name();
        final String jsonContent = commonNotificationView.json;
        final NotificationContent content = new NotificationContent(createdAt, provider, notificationType, jsonContent);
        return content;
    }

    protected void write(final List<NotificationContent> contentList) {
        logger.info(createLoggerMessage("Writing Notifications..."));
        contentList.forEach(notificationManager::saveNotification);
    }

    private Date calculateNextStartTime(final Optional<Date> latestNotificationCreatedAt, final Date currentStartDate) {
        Date newStartDate = currentStartDate;
        if (latestNotificationCreatedAt.isPresent()) {
            final Date latestNotification = latestNotificationCreatedAt.get();
            ZonedDateTime newSearchStart = ZonedDateTime.ofInstant(latestNotification.toInstant(), ZoneOffset.UTC);
            // increment 1 millisecond
            newSearchStart = newSearchStart.plusNanos(1000000);
            newStartDate = Date.from(newSearchStart.toInstant());
            logger.info(createLoggerMessage("Notifications found; updating to latest notification found"));
        } else {
            logger.info(createLoggerMessage("No notifications found; using current search time"));
        }
        return newStartDate;
    }
}
