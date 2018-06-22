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
package com.blackducksoftware.integration.hub.alert.datasource.purge;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import com.blackducksoftware.integration.hub.alert.model.NotificationModel;

@Transactional
public class PurgeReader implements ItemReader<List<NotificationModel>> {
    private final static Logger logger = LoggerFactory.getLogger(PurgeReader.class);
    private final NotificationManager notificationManager;
    private final int dayOffset;

    public PurgeReader(final NotificationManager notificationManager, final int dayOffset) {
        this.notificationManager = notificationManager;
        this.dayOffset = dayOffset;
    }

    @Override
    public List<NotificationModel> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            final Date date = createDate();
            logger.info("Searching for notifications to purge earlier than {}", date);
            final List<NotificationModel> notificationList = notificationManager.findByCreatedAtBefore(date);

            if (notificationList == null || notificationList.isEmpty()) {
                logger.info("No notifications found to purge");
                return null;
            }
            logger.info("Found {} notifications to purge", notificationList.size());
            return notificationList;
        } catch (final Exception ex) {
            logger.error("Error in Purge Reader", ex);
        }
        return null;
    }

    public Date createDate() {
        ZonedDateTime zonedDate = ZonedDateTime.now();
        zonedDate = zonedDate.minusDays(dayOffset);
        zonedDate = zonedDate.withZoneSameInstant(ZoneOffset.UTC);
        zonedDate = zonedDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date date = Date.from(zonedDate.toInstant());
        return date;
    }
}
