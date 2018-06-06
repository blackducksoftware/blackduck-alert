/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.accumulator;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class AccumulatorReader implements ItemReader<NotificationDetailResults> {
    private static final String ENCODING = "UTF-8";

    private final static Logger logger = LoggerFactory.getLogger(AccumulatorReader.class);

    private final GlobalProperties globalProperties;
    private final String lastRunPath;

    public AccumulatorReader(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
        lastRunPath = findLastRunFilePath();
    }

    private String findLastRunFilePath() {
        String path = "";
        try {
            final String configLocation = System.getenv("ALERT_CONFIG_HOME");
            final File file = new File(configLocation, "accumulator-lastrun.txt");
            path = file.getCanonicalPath();
        } catch (final IOException ex) {
            logger.error("Cannot find last run file path", ex);
        }
        return path;
    }

    @Override
    public NotificationDetailResults read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        try (RestConnection restConnection = globalProperties.createRestConnectionAndLogErrors(logger)) {
            if (restConnection != null) {
                logger.info("Accumulator Reader Starting Operation");
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection);
                final File lastRunFile = new File(lastRunPath);
                final Pair<Date, Date> dateRange = createDateRange(lastRunFile);
                final Date startDate = dateRange.getLeft();
                final Date endDate = dateRange.getRight();
                logger.info("Accumulating Notifications Between {} and {} ", RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
                final HubBucket hubBucket = new HubBucket();
                final NotificationService notificationService = hubServicesFactory.createNotificationService(true);
                final NotificationDetailResults notificationResults = notificationService.getAllNotificationDetailResultsPopulated(hubBucket, startDate, endDate);

                if (notificationResults.isEmpty()) {
                    logger.debug("Read Notification Count: 0");
                    return null;
                }
                writeNextStartTime(lastRunFile, notificationResults.getLatestNotificationCreatedAtDate(), endDate);
                logger.debug("Read Notification Count: {}", notificationResults.getResults().size());
                return notificationResults;
            }
        } catch (final Exception ex) {
            logger.error("Error in Accumulator Reader", ex);
        } finally {
            executor.shutdownNow();
            logger.info("Accumulator Reader Finished Operation");
        }
        return null;
    }

    private Pair<Date, Date> createDateRange(final File lastRunFile) {
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
                writeNextStartTime(lastRunFile, Optional.empty(), endDate);
            }
            startDate = Date.from(zonedStartDate.toInstant());

        } catch (final Exception e) {
            logger.error("Error creating date range", e);
        }

        final Pair<Date, Date> dateRange = new ImmutablePair<>(startDate, endDate);
        return dateRange;
    }

    private void writeNextStartTime(final File lastRunFile, final Optional<Date> latestNotificationCreatedAt, final Date searchEndDate) throws IOException {
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
        FileUtils.write(lastRunFile, startString, ENCODING);

    }

}
