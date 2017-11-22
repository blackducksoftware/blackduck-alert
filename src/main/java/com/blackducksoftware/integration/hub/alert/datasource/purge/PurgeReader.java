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
package com.blackducksoftware.integration.hub.alert.datasource.purge;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class PurgeReader implements ItemReader<List<NotificationEntity>> {
    private final static Logger logger = LoggerFactory.getLogger(PurgeReader.class);
    private final NotificationRepository notificationRepository;
    private final String lastRunPath;

    public PurgeReader(final NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        lastRunPath = findLastRunFilePath();
    }

    private String findLastRunFilePath() {
        String path = "";
        try {
            final String configLocation = System.getProperty("/");
            final File file = new File(configLocation, "purge-lastrun.txt");
            path = file.getCanonicalPath();
        } catch (final IOException ex) {
            logger.error("Cannot find last run file path", ex);
        }
        return path;
    }

    @Override
    public List<NotificationEntity> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            logger.debug("Purge Reader Starting Read Operation");
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
            logger.info("Searching for notifications to purge between {} and {}", startDate, endDate);
            final List<NotificationEntity> notificationList = notificationRepository.findByCreatedAtBetween(startDate, endDate);

            if (notificationList == null || notificationList.isEmpty()) {
                return null;
            }

            return notificationList;
        } catch (final Exception ex) {
            logger.error("Error in Purge Reader", ex);
        } finally {
            logger.debug("Purge Reader Finished Operation");
        }

        return null;
    }
}
