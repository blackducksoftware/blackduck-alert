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
package com.blackducksoftware.integration.alert.provider.hub.tasks;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.accumulator.Accumulator;
import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.scheduled.ScheduledTask;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

@Component
public class BlackDuckAccumulatorTask extends ScheduledTask implements Accumulator {
    public static final String DEFAULT_CRON_EXPRESSION = "0 0/1 * 1/1 * *";
    public static final String ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulatorTask.class);

    private final GlobalProperties globalProperties;
    private final List<NotificationTypeProcessor> notificationProcessors;
    private final ContentConverter contentConverter;
    private final NotificationManager notificationManager;
    private final File searchRangeFilePath;

    public BlackDuckAccumulatorTask(final TaskScheduler taskScheduler, final GlobalProperties globalProperties, final ContentConverter contentConverter,
            final NotificationManager notificationManager, final List<NotificationTypeProcessor> notificationProcessors) {
        super(taskScheduler, "blackduck-tasks");
        this.globalProperties = globalProperties;
        this.notificationProcessors = notificationProcessors;
        this.contentConverter = contentConverter;
        this.notificationManager = notificationManager;
        final String accumulatorFileName = String.format("%s-last-search.txt", getTaskName());
        this.searchRangeFilePath = new File(globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME), accumulatorFileName);
    }

    public File getSearchRangeFilePath() {
        return searchRangeFilePath;
    }

    public String formatDate(final Date date) {
        return RestConnection.formatDate(date);
    }

    public String createLoggerMessage(final String messageFormat) {
        return String.format("[ %s ] %s", getName(), messageFormat);
    }

    @Override
    public void start() {
        this.scheduleExecution(DEFAULT_CRON_EXPRESSION);
    }

    @Override
    public void stop() {
        this.scheduleExecution(STOP_SCHEDULE_EXPRESSION);
    }

    @Override
    public String getName() {
        return getTaskName();
    }

    @Override
    public void run() {
        accumulate();
    }

    @Override
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
            FileUtils.write(getSearchRangeFilePath(), nextSearchStartString, ENCODING);
        } catch (final IOException | AlertException ex) {
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

    public void initializeSearchRangeFile() throws IOException {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        FileUtils.write(getSearchRangeFilePath(), formatDate(date), ENCODING);
    }

    protected DateRange createDateRange(final File lastRunFile) throws AlertException {
        ZonedDateTime zonedEndDate = ZonedDateTime.now();
        zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
        ZonedDateTime zonedStartDate = zonedEndDate;
        final Date endDate = Date.from(zonedEndDate.toInstant());

        Date startDate = Date.from(zonedStartDate.toInstant());
        try {
            if (lastRunFile.exists()) {
                final String lastRunValue = FileUtils.readFileToString(lastRunFile, ENCODING);
                final Date startTime = RestConnection.parseDateString(lastRunValue);
                zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
            } else {
                zonedStartDate = zonedEndDate.minusMinutes(1);
            }
            startDate = Date.from(zonedStartDate.toInstant());

        } catch (final Exception e) {
            logger.error(createLoggerMessage("Error creating date range"), e);
        }

        return new DateRange(startDate, endDate);
    }

    protected Date accumulate(final DateRange dateRange) {
        final Date currentStartTime = dateRange.getStart();
        Optional<Date> latestNotificationCreatedAtDate = Optional.empty();

        final Optional<NotificationDetailResults> results = read(dateRange);
        if (results.isPresent()) {
            final AlertEvent event = process(results.get());
            write(event);
            latestNotificationCreatedAtDate = results.get().getLatestNotificationCreatedAtDate();
        }
        return calculateNextStartTime(latestNotificationCreatedAtDate, currentStartTime);
    }

    public Optional<NotificationDetailResults> read(final DateRange dateRange) {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        final Optional<RestConnection> optionalConnection = globalProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final RestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection);
                    final Date startDate = dateRange.getStart();
                    final Date endDate = dateRange.getEnd();
                    logger.info(createLoggerMessage("Accumulating Notifications Between {} and {} "), RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
                    final HubBucket hubBucket = new HubBucket();
                    final NotificationService notificationService = hubServicesFactory.createNotificationService(true);
                    final NotificationDetailResults notificationResults = notificationService.getAllNotificationDetailResultsPopulated(hubBucket, startDate, endDate);

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

    public AlertEvent process(final NotificationDetailResults notificationData) {
        logger.info(createLoggerMessage("Processing accumulated notifications"));
        final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(notificationProcessors, contentConverter);
        final AlertEvent storeEvent = notificationItemProcessor.process(globalProperties, notificationData);
        return storeEvent;
    }

    public void write(final AlertEvent event) {
        final NotificationModels notificationModels = contentConverter.getJsonContent(event.getContent(), NotificationModels.class);
        logger.info(createLoggerMessage("Writing Notifications..."));
        final List<NotificationModel> notificationList = notificationModels.getNotificationModelList();
        notificationList.forEach(notificationManager::saveNotification);
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

