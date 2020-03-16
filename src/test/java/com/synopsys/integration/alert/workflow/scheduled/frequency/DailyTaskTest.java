package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;

public class DailyTaskTest {
    private static final SchedulingDescriptorKey SCHEDULING_DESCRIPTOR_KEY = new SchedulingDescriptorKey();

    @Test
    public void testDigestType() {
        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, null, null);
        assertEquals(FrequencyType.DAILY, task.getDigestType());
    }

    @Test
    public void testGetTaskName() {
        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, null, null);
        assertEquals(ScheduledTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    public void cronExpressionNotDefault() throws AlertDatabaseConstraintException {
        final String notDefaultValue = "44";
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, null, null, ConfigContextEnum.GLOBAL);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
        configurationFieldModel.setFieldValue(notDefaultValue);
        configurationModel.put(configurationFieldModel);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));

        DailyTask task = new DailyTask(SCHEDULING_DESCRIPTOR_KEY, null, null, null, null, null, configurationAccessor);
        String cronWithNotDefault = task.scheduleCronExpression();
        String expectedCron = String.format(DailyTask.CRON_FORMAT, notDefaultValue);

        assertEquals(expectedCron, cronWithNotDefault);
    }

}
