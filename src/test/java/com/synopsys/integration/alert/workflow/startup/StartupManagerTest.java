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

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;
import com.synopsys.integration.alert.workflow.update.UpdateNotifierTask;

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
    public void testLogConfiguration() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TaskManager taskManager = new TaskManager();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);

        Mockito.when(proxyManager.getProxyHost()).thenReturn(Optional.of("google.com"));
        Mockito.when(proxyManager.getProxyPort()).thenReturn(Optional.of("3218"));
        Mockito.when(proxyManager.getProxyUsername()).thenReturn(Optional.of("AUser"));
        Mockito.when(proxyManager.getProxyPassword()).thenReturn(Optional.of("aPassword"));

        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck Url");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        final TestBlackDuckProperties mockTestGlobalProperties = Mockito.spy(testGlobalProperties);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final StartupManager startupManager = new StartupManager(testAlertProperties, mockTestGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility, systemValidator, baseConfigurationAccessor, proxyManager,
            taskManager, samlManager);

        startupManager.logConfiguration();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
        assertTrue(outputLogger.isLineContainingText("BlackDuck API Token:           **********"));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Timeout:             300"));
    }

    @Test
    public void testInitializeCronJobsWithEmptyConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TaskManager taskManager = new TaskManager();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
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
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(schedulingModel);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, updateNotifierTask, null, Collections.emptyList(), systemStatusUtility, systemValidator,
            baseConfigurationAccessor, proxyManager, taskManager, samlManager);
        //        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsWithConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TaskManager taskManager = new TaskManager();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
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

        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, updateNotifierTask, null, Collections.emptyList(), systemStatusUtility, systemValidator,
            baseConfigurationAccessor, proxyManager, taskManager, samlManager);
        //        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor, Mockito.times(0)).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsUpdateConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TaskManager taskManager = new TaskManager();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
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
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))).thenReturn(Optional.empty());
        Mockito.when(schedulingModel.getField(Mockito.eq(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS))).thenReturn(Optional.empty());
        final List<ConfigurationModel> configList = List.of(schedulingModel);
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);
        Mockito.when(baseConfigurationAccessor.updateConfiguration(Mockito.anyLong(), Mockito.anyCollection())).thenReturn(schedulingModel);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);

        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, updateNotifierTask, null, Collections.emptyList(), systemStatusUtility, systemValidator,
            baseConfigurationAccessor, proxyManager, taskManager, samlManager);
        //        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

    @Test
    public void testInitializeCronJobsMissingConfig() throws Exception {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TaskManager taskManager = new TaskManager();
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        final UpdateNotifierTask updateNotifierTask = Mockito.mock(UpdateNotifierTask.class);
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
        final ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final ConfigurationModel schedulingModel = Mockito.mock(ConfigurationModel.class);

        final List<ConfigurationModel> configList = List.of();
        Mockito.when(baseConfigurationAccessor.getConfigurationsByDescriptorName(SchedulingDescriptor.SCHEDULING_COMPONENT)).thenReturn(configList);
        Mockito.when(baseConfigurationAccessor.createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(schedulingModel);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, updateNotifierTask, null, Collections.emptyList(), systemStatusUtility, systemValidator,
            baseConfigurationAccessor, proxyManager, taskManager, samlManager);
        //        startupManager.registerDescriptors();
        startupManager.initializeCronJobs();

        Mockito.verify(baseConfigurationAccessor).createConfiguration(Mockito.anyString(), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection());
    }
}
