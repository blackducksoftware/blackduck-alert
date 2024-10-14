/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling.workflow;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.message.model.DateRange;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.blackduck.integration.alert.component.tasks.ProcessingTask;

@Component
public class DailyTask extends ProcessingTask {
    public static final String CRON_FORMAT = "0 0 %s 1/1 * ?";
    public static final int DEFAULT_HOUR_OF_DAY = 0;

    private final SchedulingDescriptorKey schedulingDescriptorKey;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public DailyTask(
        SchedulingDescriptorKey schedulingDescriptorKey,
        TaskScheduler taskScheduler,
        NotificationAccessor notificationAccessor,
        NotificationMappingProcessor notificationMappingProcessor,
        TaskManager taskManager,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        JobAccessor jobAccessor,
        EventManager eventManager
    ) {
        super(taskScheduler, taskManager, notificationAccessor, notificationMappingProcessor, jobAccessor, FrequencyType.DAILY, eventManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public String scheduleCronExpression() {
        String dailySavedCronValue = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(schedulingDescriptorKey)
            .stream()
            .findFirst()
            .flatMap(configurationModel -> configurationModel.getField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(String.valueOf(DEFAULT_HOUR_OF_DAY));
        return String.format(CRON_FORMAT, dailySavedCronValue);
    }

    @Override
    public DateRange getDateRange() {
        // the hour of the daily task is configurable so zero out the minutes, seconds, and nanoseconds.
        OffsetDateTime endDate = DateUtils.createCurrentDateTimestamp()
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        OffsetDateTime startDate = endDate.minusDays(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

        return DateRange.of(startDate, endDate);
    }
}
