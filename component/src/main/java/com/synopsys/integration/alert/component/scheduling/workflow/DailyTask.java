/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.component.tasks.ProcessingTask;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;

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
        NotificationProcessor notificationProcessor,
        TaskManager taskManager,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        JobAccessor jobAccessor
    ) {
        super(taskScheduler, taskManager, notificationAccessor, notificationProcessor, jobAccessor, FrequencyType.DAILY);
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

}
