/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.blackduck.integration.alert.component.scheduling.workflow.DailyTask;
import com.blackduck.integration.alert.database.job.api.StaticJobAccessor;

class DailyTaskTest {
    private static final SchedulingDescriptorKey SCHEDULING_DESCRIPTOR_KEY = new SchedulingDescriptorKey();

    @Test
    void testGetTaskName() {
        EventManager eventManager = Mockito.mock(EventManager.class);
        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, null, null, eventManager);
        assertEquals(ScheduledTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    void cronExpressionNotDefault() {
        final String notDefaultValue = "44";
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, null, null, ConfigContextEnum.GLOBAL);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
        configurationFieldModel.setFieldValue(notDefaultValue);
        configurationModel.put(configurationFieldModel);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.hasJobsByFrequency(Mockito.any())).thenReturn(true);

        EventManager eventManager = Mockito.mock(EventManager.class);
        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, configurationModelConfigurationAccessor, jobAccessor, eventManager);
        String cronWithNotDefault = task.scheduleCronExpression();
        String expectedCron = String.format(DailyTask.CRON_FORMAT, notDefaultValue);

        assertEquals(expectedCron, cronWithNotDefault);
    }

}
