/**
 * workflow
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.scheduled.frequency;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.rest.RestConstants;

public abstract class ProcessingTask extends StartupScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private NotificationAccessor notificationAccessor;
    private NotificationProcessor notificationProcessor;
    private ChannelEventManager eventManager;
    private OffsetDateTime lastRunTime;

    public ProcessingTask(TaskScheduler taskScheduler, NotificationAccessor notificationAccessor, NotificationProcessor notificationProcessor, ChannelEventManager eventManager, TaskManager taskManager) {
        super(taskScheduler, taskManager);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
        lastRunTime = DateUtils.createCurrentDateTimestamp();
    }

    public abstract FrequencyType getDigestType();

    public OffsetDateTime getLastRunTime() {
        return lastRunTime;
    }

    public DateRange getDateRange() {
        OffsetDateTime startDate = lastRunTime;
        OffsetDateTime endDate = DateUtils.createCurrentDateTimestamp();
        return DateRange.of(startDate, endDate);
    }

    @Override
    public void runTask() {
        DateRange dateRange = getDateRange();
        List<AlertNotificationModel> notificationList = read(dateRange);
        List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(getDigestType(), notificationList);
        eventManager.sendEvents(distributionEvents);
        lastRunTime = DateUtils.createCurrentDateTimestamp();
    }

    public List<AlertNotificationModel> read(DateRange dateRange) {
        try {
            String taskName = getTaskName();
            OffsetDateTime startDate = dateRange.getStart();
            OffsetDateTime endDate = dateRange.getEnd();
            logger.info("{} Reading Notifications Between {} and {} ", taskName, DateUtils.formatDate(startDate, RestConstants.JSON_DATE_FORMAT), DateUtils.formatDate(endDate, RestConstants.JSON_DATE_FORMAT));
            List<AlertNotificationModel> entityList = notificationAccessor.findByCreatedAtBetween(startDate, endDate);
            if (entityList.isEmpty()) {
                logger.info("{} Notifications Found: 0", taskName);
                return List.of();
            } else {
                logger.info("{} Notifications Found: {}", taskName, entityList.size());
                return entityList;
            }
        } catch (Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        }
        return List.of();
    }

}
