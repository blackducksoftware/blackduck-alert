/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.workflow.scheduled.frequency.ProcessingTask;

@Component
public class DailyTask extends ProcessingTask {
    public static final String CRON_FORMAT = "0 0 %s 1/1 * ?";
    public static final int DEFAULT_HOUR_OF_DAY = 0;

    private final SchedulingDescriptorKey schedulingDescriptorKey;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public DailyTask(SchedulingDescriptorKey schedulingDescriptorKey, TaskScheduler taskScheduler, NotificationAccessor notificationAccessor, NotificationProcessor notificationProcessor, ChannelEventManager eventManager,
        TaskManager taskManager, ConfigurationAccessor configurationAccessor) {
        super(taskScheduler, notificationAccessor, notificationProcessor, eventManager, taskManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public FrequencyType getDigestType() {
        return FrequencyType.DAILY;
    }

    @Override
    public String scheduleCronExpression() {
        String dailySavedCronValue = configurationAccessor.getConfigurationsByDescriptorKey(schedulingDescriptorKey)
                                         .stream()
                                         .findFirst()
                                         .flatMap(configurationModel -> configurationModel.getField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))
                                         .flatMap(ConfigurationFieldModel::getFieldValue)
                                         .orElse(String.valueOf(DEFAULT_HOUR_OF_DAY));
        return String.format(CRON_FORMAT, dailySavedCronValue);
    }

}
