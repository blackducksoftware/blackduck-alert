/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class AccumulatorReader implements ItemReader<NotificationResults> {
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
            final String configLocation = System.getProperty("/");
            final File file = new File(configLocation, "accumulator-lastrun.txt");
            path = file.getCanonicalPath();
        } catch (final IOException ex) {
            logger.error("Cannot find last run file path", ex);
        }
        return path;
    }

    @Override
    public NotificationResults read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryAndLogErrors(logger);
            if (hubServicesFactory != null) {
                logger.debug("Accumulator Reader Starting Read Operation");
                ZonedDateTime zonedEndDate = ZonedDateTime.now();
                zonedEndDate = zonedEndDate.withZoneSameInstant(ZoneOffset.UTC);
                zonedEndDate = zonedEndDate.withSecond(0).withNano(0);
                ZonedDateTime zonedStartDate = zonedEndDate;
                final Date endDate = Date.from(zonedEndDate.toInstant());
                Date startDate = Date.from(zonedStartDate.toInstant());
                try {
                    final File lastRunFile = new File(lastRunPath);
                    if (lastRunFile.exists()) {
                        final String lastRunValue = FileUtils.readFileToString(lastRunFile, "UTF-8");
                        final Date startTime = RestConnection.parseDateString(lastRunValue);
                        zonedStartDate = ZonedDateTime.ofInstant(startTime.toInstant(), zonedEndDate.getZone());
                    } else {
                        zonedStartDate = zonedEndDate;
                    }
                    zonedStartDate = zonedStartDate.withSecond(0).withNano(0);
                    startDate = Date.from(zonedStartDate.toInstant());
                    FileUtils.write(lastRunFile, RestConnection.formatDate(endDate), "UTF-8");
                } catch (final Exception e) {
                    logger.error("Error creating date range", e);
                }

                final NotificationDataService notificationDataService = hubServicesFactory.createNotificationDataService();
                final NotificationResults notificationResults = notificationDataService.getAllNotifications(startDate, endDate);

                if (notificationResults.getNotificationContentItems().isEmpty()) {
                    logger.debug("Read Notification Count: 0");
                    return null;
                }
                logger.debug("Read Notification Count: {}", notificationResults.getNotificationContentItems().size());
                return notificationResults;
            }
        } catch (final Exception ex) {
            logger.error("Error in Accumulator Reader", ex);
        } finally {
            logger.debug("Accumulator Reader Finished Operation");
        }
        return null;
    }

}
