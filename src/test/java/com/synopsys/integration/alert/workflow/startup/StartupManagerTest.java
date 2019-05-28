package com.synopsys.integration.alert.workflow.startup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.update.UpdateNotifierTask;
import com.synopsys.integration.alert.workflow.startup.component.CronJobsStartupComponent;

public class StartupManagerTest {

    @Test
    public void testInitializeCronJobsWithEmptyConfig() throws Exception {
        final TaskManager taskManager = new TaskManager();
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(schedulingModel);
        final CronJobsStartupComponent cronJobsStartupComponent = new CronJobsStartupComponent(baseConfigurationAccessor, taskManager, purgeTask, dailyTask, phoneHomeTask, updateNotifierTask);
        cronJobsStartupComponent.run();

        Mockito.verify(baseConfigurationAccessor).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsWithConfig() throws Exception {
        final TaskManager taskManager = new TaskManager();
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);
        final Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        final ConfigurationFieldModel hourOfDayField = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
        hourOfDayField.setFieldValue("1");
        configuredFields.put(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, hourOfDayField);
        final ConfigurationFieldModel purgeFrequencyField = ConfigurationFieldModel.create(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
        purgeFrequencyField.setFieldValue("2");
        configuredFields.put(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, purgeFrequencyField);
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))).thenReturn(Optional.of(hourOfDayField));
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS))).thenReturn(Optional.of(purgeFrequencyField));
        Mockito.when(schedulingModel.getCopyOfKeyToFieldMap()).thenReturn(configuredFields);
        final List<ConfigurationModel> configList = List.of(schedulingModel);
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);

        final CronJobsStartupComponent cronJobsStartupComponent = new CronJobsStartupComponent(baseConfigurationAccessor, taskManager, purgeTask, dailyTask, phoneHomeTask, updateNotifierTask);
        cronJobsStartupComponent.run();

        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsUpdateConfig() throws Exception {
        final TaskManager taskManager = new TaskManager();
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))).thenReturn(Optional.empty());
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS))).thenReturn(Optional.empty());
        final List<ConfigurationModel> configList = List.of(schedulingModel);
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);
        Mockito.when(baseConfigurationAccessor.updateConfiguration(Mockito.anyLong(), Mockito.anyCollection())).thenReturn(schedulingModel);

        final CronJobsStartupComponent cronJobsStartupComponent = new CronJobsStartupComponent(baseConfigurationAccessor, taskManager, purgeTask, dailyTask, phoneHomeTask, updateNotifierTask);
        cronJobsStartupComponent.run();

        Mockito.verify(baseConfigurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsMissingConfig() throws Exception {
        final TaskManager taskManager = new TaskManager();
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);

        final List<ConfigurationModel> configList = List.of();
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);
        Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(schedulingModel);
        final CronJobsStartupComponent cronJobsStartupComponent = new CronJobsStartupComponent(baseConfigurationAccessor, taskManager, purgeTask, dailyTask, phoneHomeTask, updateNotifierTask);
        cronJobsStartupComponent.run();

        Mockito.verify(baseConfigurationAccessor).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }
}
