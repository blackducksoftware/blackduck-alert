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
package com.blackducksoftware.integration.hub.alert.digest;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.rest.connection.RestConnection;

@Transactional
public abstract class DigestItemReader implements ItemReader<List<NotificationModel>> {
    private final Logger logger = LoggerFactory.getLogger(DigestItemReader.class);
    private final NotificationManager notificationManager;
    private boolean hasRead;
    private final String readerName;

    public DigestItemReader(final String readerName, final NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        hasRead = false;
        this.readerName = readerName;
    }

    @Override
    public List<NotificationModel> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            logger.info("{} Digest Item Reader started...", readerName);
            if (hasRead) {
                return null;
            } else {
                final DateRange dateRange = getDateRange();
                final Date startDate = dateRange.getStart();
                final Date endDate = dateRange.getEnd();
                logger.info("{} Digest Item Reader Finding Notifications Between {} and {} ", readerName, RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
                final List<NotificationModel> entityList = notificationManager.findByCreatedAtBetween(startDate, endDate);
                hasRead = true;
                if (entityList.isEmpty()) {
                    logger.info("{} Digest Item Reader Notification Count: 0", readerName);
                    return null;
                } else {
                    logger.info("{} Digest Item Reader Notification Count: {}", readerName, entityList.size());
                    return entityList;
                }
            }
        } catch (final Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        } finally {
            logger.info("{} Digest Item Reader Finished Operation", readerName);
        }
        return null;
    }

    public abstract DateRange getDateRange();

}
