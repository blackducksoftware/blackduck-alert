package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.scheduling.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.SchedulingUIConfig;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;

public class StartupManagerTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testLogConfiguration() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final TestBlackDuckProperties mockTestGlobalProperties = Mockito.spy(testGlobalProperties);
        testAlertProperties.setAlertProxyPassword("not_blank_data");
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final StartupManager startupManager = new StartupManager(testAlertProperties, mockTestGlobalProperties, null, null, null, null, null, null, systemStatusUtility, systemValidator, baseConfigurationAccessor, encryptionUtility
            , Collections.emptyList(), baseDescriptorAccessor);

        startupManager.logConfiguration();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }

    @Test
    public void testInitializeCronJobsWithEmptyConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();

        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final OnDemandTask onDemandTask = Mockito.mock(OnDemandTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final ConfigurationAccessor.ConfigurationModel schedulingModel = Mockito.mock(ConfigurationAccessor.ConfigurationModel.class);
        Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(schedulingModel);
        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, null, Collections.emptyList(), systemStatusUtility, systemValidator, baseConfigurationAccessor,
            encryptionUtility, Collections.emptyList(), baseDescriptorAccessor);
        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsWithConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();

        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final DailyTask dailyTask = Mockito.mock(DailyTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final OnDemandTask onDemandTask = Mockito.mock(OnDemandTask.class);
        Mockito.doNothing().when(dailyTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(dailyTask).getFormatedNextRunTime();
        final PurgeTask purgeTask = Mockito.mock(PurgeTask.class);
        Mockito.doNothing().when(purgeTask).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(Optional.of("time")).when(purgeTask).getFormatedNextRunTime();
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);
        final ConfigurationAccessor.ConfigurationModel schedulingModel = Mockito.mock(ConfigurationAccessor.ConfigurationModel.class);
        final Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        final ConfigurationFieldModel hourOfDayField = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY);
        hourOfDayField.setFieldValue("1");
        configuredFields.put(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY, hourOfDayField);
        final ConfigurationFieldModel purgeFrequencyField = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS);
        purgeFrequencyField.setFieldValue("2");
        configuredFields.put(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS, purgeFrequencyField);
        Mockito.when(schedulingModel.getCopyOfKeyToFieldMap()).thenReturn(configuredFields);
        final List<ConfigurationAccessor.ConfigurationModel> configList = List.of(schedulingModel);
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);

        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, null, Collections.emptyList(), systemStatusUtility, systemValidator, baseConfigurationAccessor,
            encryptionUtility, Collections.emptyList(), baseDescriptorAccessor);
        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }
}
