package com.synopsys.integration.alert.workflow.scheduled;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.component.scheduling.workflow.PurgeTask;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class PurgeTaskTest {

    @Test
    public void cronExpressionNotDefault() {
        final String notDefaultValue = "44";
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, null, null, ConfigContextEnum.GLOBAL);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
        configurationFieldModel.setFieldValue(notDefaultValue);
        configurationModel.put(configurationFieldModel);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.any(DescriptorKey.class))).thenReturn(List.of(configurationModel));
        PurgeTask task = new PurgeTask(new SchedulingDescriptorKey(), null, null, null, null, configurationAccessor);
        String cronWithNotDefault = task.scheduleCronExpression();
        String expectedCron = String.format(PurgeTask.CRON_FORMAT, notDefaultValue);

        assertEquals(expectedCron, cronWithNotDefault);
    }

}
