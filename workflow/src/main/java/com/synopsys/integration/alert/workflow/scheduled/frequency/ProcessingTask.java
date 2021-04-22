/*
 * workflow
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.scheduled.frequency;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;

public abstract class ProcessingTask extends StartupScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationAccessor notificationAccessor;
    private final NotificationProcessor notificationProcessor;
    private final FrequencyType frequencyType;

    private OffsetDateTime lastRunTime;

    public ProcessingTask(
        TaskScheduler taskScheduler,
        NotificationAccessor notificationAccessor,
        TaskManager taskManager,
        NotificationProcessor notificationProcessor,
        FrequencyType frequencyType
    ) {
        super(taskScheduler, taskManager);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.frequencyType = frequencyType;
        lastRunTime = DateUtils.createCurrentDateTimestamp();
    }

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
        logger.info("Processing {} notifications.", notificationList.size());
        notificationProcessor.processNotifications(notificationList, List.of(frequencyType));
        lastRunTime = DateUtils.createCurrentDateTimestamp();
    }

    public List<AlertNotificationModel> read(DateRange dateRange) {
        try {
            String taskName = getTaskName();
            OffsetDateTime startDate = dateRange.getStart();
            OffsetDateTime endDate = dateRange.getEnd();
            logger.info("{} Reading Notifications Between {} and {} ", taskName, DateUtils.formatDateAsJsonString(startDate), DateUtils.formatDateAsJsonString(endDate));
            List<AlertNotificationModel> entityList = notificationAccessor.findByCreatedAtBetween(startDate, endDate);
            if (entityList.isEmpty()) {
                return List.of();
            } else {
                return entityList;
            }
        } catch (Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        }
        return List.of();
    }

}
