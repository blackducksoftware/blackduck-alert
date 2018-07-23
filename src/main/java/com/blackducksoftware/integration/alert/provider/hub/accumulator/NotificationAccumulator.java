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
package com.blackducksoftware.integration.alert.provider.hub.accumulator;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.accumulator.PollingAccumulator;
import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.common.enumeration.InternalEventTypes;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

@Component
public class NotificationAccumulator extends PollingAccumulator {
    public static final String DEFAULT_CRON_EXPRESSION = "0 0/1 * 1/1 * *";

    private static final Logger logger = LoggerFactory.getLogger(NotificationAccumulator.class);

    private final GlobalProperties globalProperties;
    private final List<NotificationTypeProcessor> notificationProcessors;
    private final ContentConverter contentConverter;
    private final NotificationManager notificationManager;
    private final ChannelTemplateManager channelTemplateManager;

    public NotificationAccumulator(final TaskScheduler taskScheduler, final GlobalProperties globalProperties, final ContentConverter contentConverter,
            final NotificationManager notificationManager, final ChannelTemplateManager channelTemplateManager, final List<NotificationTypeProcessor> notificationProcessors) {
        super(taskScheduler, "blackduck", DEFAULT_CRON_EXPRESSION, globalProperties.getEnvironmentVariable(AlertEnvironment.ALERT_CONFIG_HOME.getVariableName()));
        this.globalProperties = globalProperties;
        this.notificationProcessors = notificationProcessors;
        this.contentConverter = contentConverter;
        this.notificationManager = notificationManager;
        this.channelTemplateManager = channelTemplateManager;
    }

    @Override
    protected Pair<Date, Date> createDateRange(final File lastRunFile) throws AlertException {
        ZonedDateTime zonedEndDate = ZonedDateTime.now();
        zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
        ZonedDateTime zonedStartDate = zonedEndDate;
        final Date endDate = Date.from(zonedEndDate.toInstant());

        Date startDate = Date.from(zonedStartDate.toInstant());
        try {
            if (lastRunFile.exists()) {
                final String lastRunValue = FileUtils.readFileToString(lastRunFile, PollingAccumulator.ENCODING);
                final Date startTime = RestConnection.parseDateString(lastRunValue);
                zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
            } else {
                zonedStartDate = zonedEndDate.minusMinutes(1);
            }
            startDate = Date.from(zonedStartDate.toInstant());

        } catch (final Exception e) {
            logger.error("Error creating date range", e);
        }

        final Pair<Date, Date> dateRange = new ImmutablePair<>(startDate, endDate);
        return dateRange;
    }

    @Override
    protected String accumulate(final Pair<Date, Date> dateRange) throws AlertException {
        final Optional<NotificationDetailResults> results = read(dateRange);
        final Optional<AlertEvent> event = process(results);
        write(event);
        if (results.isPresent()) {
            final Optional<Date> latestNotificationCreatedAtDate = results.get().getLatestNotificationCreatedAtDate();
            return calculateNextStartTime(getSearchRangeFilePath(), latestNotificationCreatedAtDate, dateRange.getRight());
        } else {
            return calculateNextStartTime(getSearchRangeFilePath(), Optional.empty(), dateRange.getRight());
        }
    }

    public Optional<NotificationDetailResults> read(final Pair<Date, Date> dateRange) {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        try (final RestConnection restConnection = globalProperties.createRestConnectionAndLogErrors(logger)) {
            if (restConnection != null) {
                logger.info("Accumulator Reader Starting Operation");
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection);
                final Date startDate = dateRange.getLeft();
                final Date endDate = dateRange.getRight();
                logger.info("Accumulating Notifications Between {} and {} ", RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
                final HubBucket hubBucket = new HubBucket();
                final NotificationService notificationService = hubServicesFactory.createNotificationService(true);
                final NotificationDetailResults notificationResults = notificationService.getAllNotificationDetailResultsPopulated(hubBucket, startDate, endDate);

                if (notificationResults.isEmpty()) {
                    logger.debug("Read Notification Count: 0");
                    return Optional.empty();
                }
                logger.debug("Read Notification Count: {}", notificationResults.getResults().size());
                return Optional.of(notificationResults);
            }
        } catch (final Exception ex) {
            logger.error("Error in Accumulator Reader", ex);
        } finally {
            executor.shutdownNow();
            logger.info("Accumulator Reader Finished Operation");
        }
        return Optional.empty();
    }

    public Optional<AlertEvent> process(final Optional<NotificationDetailResults> optionalData) {
        if (optionalData.isPresent()) {
            final NotificationDetailResults notificationData = optionalData.get();
            logger.info("Processing accumulated notifications");
            final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(notificationProcessors, contentConverter);
            final AlertEvent storeEvent = notificationItemProcessor.process(globalProperties, notificationData);
            return Optional.of(storeEvent);
        } else {
            logger.info("No notifications to process");
            return Optional.empty();
        }
    }

    public void write(final Optional<AlertEvent> optionalEvent) {
        if (optionalEvent.isPresent()) {
            final AlertEvent event = optionalEvent.get();
            final Optional<NotificationModels> optionalModel = contentConverter.getContent(event.getContent(), NotificationModels.class);
            if (optionalModel.isPresent()) {
                final NotificationModels notificationModels = optionalModel.get();
                final List<NotificationModel> notificationList = notificationModels.getNotificationModelList();
                final List<NotificationModel> entityList = notificationList.stream().map(notificationManager::saveNotification).collect(Collectors.toList());
                final AlertEvent realTimeEvent = new AlertEvent(InternalEventTypes.REAL_TIME_EVENT.getDestination(), contentConverter.convertToString(new NotificationModels(entityList)));
                channelTemplateManager.sendEvent(realTimeEvent);
            }
        }
    }

    private String calculateNextStartTime(final File lastRunFile, final Optional<Date> latestNotificationCreatedAt, final Date searchEndDate) {
        final String startString;
        if (latestNotificationCreatedAt.isPresent()) {
            final Date latestNotification = latestNotificationCreatedAt.get();
            ZonedDateTime newSearchStart = ZonedDateTime.ofInstant(latestNotification.toInstant(), ZoneOffset.UTC);
            // increment 1 millisecond
            newSearchStart = newSearchStart.plusNanos(1000000);
            final Date newSearchStartDate = Date.from(newSearchStart.toInstant());
            startString = RestConnection.formatDate(newSearchStartDate);
            logger.debug("Last Notification Read Timestamp Found");
        } else {
            startString = RestConnection.formatDate(searchEndDate);
            logger.debug("Last Notification Read Timestamp Not Found");
        }
        logger.info("Accumulator Next Range Start Time: {} ", startString);
        return startString;
    }
}

