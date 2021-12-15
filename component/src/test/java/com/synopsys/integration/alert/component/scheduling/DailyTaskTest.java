package com.synopsys.integration.alert.component.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.task.ScheduledTask;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.component.scheduling.workflow.DailyTask;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

class DailyTaskTest {
    private static final SchedulingDescriptorKey SCHEDULING_DESCRIPTOR_KEY = new SchedulingDescriptorKey();

    @Test
    void testGetTaskName() {
        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, null, null);
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


        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, configurationModelConfigurationAccessor, jobAccessor);
        String cronWithNotDefault = task.scheduleCronExpression();
        String expectedCron = String.format(DailyTask.CRON_FORMAT, notDefaultValue);

        assertEquals(expectedCron, cronWithNotDefault);
    }

}
