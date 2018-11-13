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
package com.synopsys.integration.alert.workflow.scheduled;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.workflow.NotificationManager;

@Component
public class PurgeTask extends ScheduledTask {
    private static final int DEFAULT_DAY_OFFSET = 1;
    private final Logger logger = LoggerFactory.getLogger(PurgeTask.class);
    private final NotificationManager notificationManager;
    private final SystemMessageUtility systemMessageUtility;
    private int dayOffset;

    @Autowired
    public PurgeTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager, final SystemMessageUtility systemMessageUtility) {
        super(taskScheduler, "purge-task");
        this.notificationManager = notificationManager;
        this.systemMessageUtility = systemMessageUtility;
        this.dayOffset = 1;
    }

    @Override
    public void run() {
        purgeNotifications();
        purgeSystemMessages();
    }

    public void setDayOffset(final int dayOffset) {
        this.dayOffset = dayOffset;
    }

    public void resetDayOffset() {
        setDayOffset(DEFAULT_DAY_OFFSET);
    }

    private void purgeNotifications() {
        try {
            final Date date = createDate();
            logger.info("Searching for notifications to purge earlier than {}", date);
            final List<NotificationContent> notifications = notificationManager.findByCreatedAtBefore(date);

            if (notifications == null || notifications.isEmpty()) {
                logger.info("No notifications found to purge");
            } else {
                logger.info("Found {} notifications to purge", notifications.size());
                logger.info("Purging {} notifications.", notifications.size());
                notificationManager.deleteNotificationList(notifications);
            }
        } catch (final Exception ex) {
            logger.error("Error in purging notifications", ex);
        }
    }

    private void purgeSystemMessages() {
        try {
            final Date date = createDate();
            final List<SystemMessage> messages = systemMessageUtility.getSystemMessagesBefore(date);
            systemMessageUtility.deleteSystemMessages(messages);
        } catch (final Exception ex) {
            logger.error("Error purging system messages", ex);
        }
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
