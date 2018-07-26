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
package com.blackducksoftware.integration.alert.workflow.scheduled.frequency;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.digest.DigestNotificationProcessor;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.alert.workflow.scheduled.ScheduledTask;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public abstract class ProcessingTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationManager notificationManager;
    private final DigestNotificationProcessor notificationProcessor;
    private final ChannelTemplateManager channelTemplateManager;

    public ProcessingTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager, final DigestNotificationProcessor notificationProcessor, final ChannelTemplateManager channelTemplateManager) {
        super(taskScheduler);
        this.notificationManager = notificationManager;
        this.notificationProcessor = notificationProcessor;
        this.channelTemplateManager = channelTemplateManager;
    }

    public abstract DateRange getDateRange();

    public abstract DigestType getDigestType();

    public String getTaskName() {
        return getClass().getName();
    }

    @Override
    public void run() {
        final DateRange dateRange = getDateRange();
        final List<NotificationModel> modelList = read(dateRange);
        final List<ChannelEvent> eventList = process(modelList);
        channelTemplateManager.sendEvents(eventList);
    }

    private List<NotificationModel> read(final DateRange dateRange) {

        final String taskName = getTaskName();
        try {
            logger.info("{} Digest Item Reader started...", taskName);
            final Date startDate = dateRange.getStart();
            final Date endDate = dateRange.getEnd();
            logger.info("{} Digest Item Reader Finding Notifications Between {} and {} ", taskName, RestConnection.formatDate(startDate), RestConnection.formatDate(endDate));
            final List<NotificationModel> entityList = notificationManager.findByCreatedAtBetween(startDate, endDate);
            if (entityList.isEmpty()) {
                logger.info("{} Digest Item Reader Notification Count: 0", taskName);
                return Collections.emptyList();
            } else {
                logger.info("{} Digest Item Reader Notification Count: {}", taskName, entityList.size());
                return entityList;
            }
        } catch (final Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        } finally {
            logger.info("{} Digest Item Reader Finished Operation", taskName);
        }
        return Collections.emptyList();
    }

    private List<ChannelEvent> process(final List<NotificationModel> modelList) {
        try {
            logger.info("Notification Entity Count: {}", modelList.size());
            final List<ChannelEvent> events = notificationProcessor.processNotifications(getDigestType(), modelList);
            if (events.isEmpty()) {
                return Collections.emptyList();
            } else {
                return events;
            }
        } catch (final Exception ex) {
            logger.error("Error processing digest notifications", ex);
        }
        return Collections.emptyList();
    }
}
