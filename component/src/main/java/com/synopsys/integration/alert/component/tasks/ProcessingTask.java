/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.task.StartupScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.processor.api.NotificationMappingProcessor;
import com.synopsys.integration.alert.processor.api.event.JobNotificationMappedEvent;

public abstract class ProcessingTask extends StartupScheduledTask {
    public static final int PAGE_SIZE = 100;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationAccessor notificationAccessor;
    private final NotificationMappingProcessor notificationMappingProcessor;
    private final JobAccessor jobAccessor;
    private final FrequencyType frequencyType;
    private final EventManager eventManager;

    private OffsetDateTime lastRunTime;

    protected ProcessingTask(
        TaskScheduler taskScheduler,
        TaskManager taskManager,
        NotificationAccessor notificationAccessor,
        NotificationMappingProcessor notificationMappingProcessor,
        JobAccessor jobAccessor,
        FrequencyType frequencyType,
        EventManager eventManager
    ) {
        super(taskScheduler, taskManager);
        this.notificationAccessor = notificationAccessor;
        this.notificationMappingProcessor = notificationMappingProcessor;
        this.jobAccessor = jobAccessor;
        this.frequencyType = frequencyType;
        this.eventManager = eventManager;
        lastRunTime = DateUtils.createCurrentDateTimestamp();
    }

    public OffsetDateTime getLastRunTime() {
        return lastRunTime;
    }

    public abstract DateRange getDateRange();
    @Override
    public void runTask() {
        boolean hasJobsByFrequency = jobAccessor.hasJobsByFrequency(frequencyType.name());
        String taskName = getTaskName();
        if (hasJobsByFrequency) {
            // Need to capture time before processing so next iteration will begin with correct time
            OffsetDateTime iterationTimeStamp = DateUtils.createCurrentDateTimestamp();
            logger.info("{} Jobs with Daily Frequency found.  Begin processing notifications", taskName);
            process();
            lastRunTime = iterationTimeStamp;
        } else {
            logger.info("{} Jobs with Daily Frequency not found.", taskName);
        }
    }

    private void process() {
        DateRange dateRange = getDateRange();
        AlertPagedModel<AlertNotificationModel> page = read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, PAGE_SIZE);
        int currentPage = page.getCurrentPage();
        int totalPages = page.getTotalPages();
        UUID correlationId = UUID.randomUUID();
        while (!page.getModels().isEmpty() || currentPage < totalPages) {
            List<AlertNotificationModel> notificationList = page.getModels();
            logger.info("Processing page {} of {}. {} notifications to process.", currentPage, totalPages, notificationList.size());
            notificationMappingProcessor.processNotifications(correlationId, notificationList, List.of(frequencyType));
            eventManager.sendEvent(new JobNotificationMappedEvent(correlationId));
            page = read(dateRange, currentPage + 1, PAGE_SIZE);
            currentPage = page.getCurrentPage();
            totalPages = page.getTotalPages();
        }
    }

    public AlertPagedModel<AlertNotificationModel> read(DateRange dateRange, int pageNumber, int pageSize) {
        try {
            String taskName = getTaskName();
            OffsetDateTime startDate = dateRange.getStart();
            OffsetDateTime endDate = dateRange.getEnd();
            logger.info("{} Reading Notifications Between {} and {} ", taskName, DateUtils.formatDateAsJsonString(startDate), DateUtils.formatDateAsJsonString(endDate));
            return notificationAccessor.findByCreatedAtBetween(startDate, endDate, pageNumber, pageSize);
        } catch (Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        }
        return new AlertPagedModel<>(0, pageNumber, pageSize, List.of());
    }

}
