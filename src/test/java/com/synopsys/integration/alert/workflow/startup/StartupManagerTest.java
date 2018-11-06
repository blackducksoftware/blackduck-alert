package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingRepository;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.web.scheduling.mock.MockGlobalSchedulingEntity;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;

public class StartupManagerTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
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
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, mockTestGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);

        startupManager.logConfiguration();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }

    @Test
    public void testInitializeCronJobs() throws IOException {
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
        final SchedulingRepository schedulingRepository = Mockito.mock(SchedulingRepository.class);
        final MockGlobalSchedulingEntity mockGlobalSchedulingEntity = new MockGlobalSchedulingEntity();
        final SchedulingConfigEntity entity = mockGlobalSchedulingEntity.createGlobalEntity();
        Mockito.when(schedulingRepository.save(Mockito.any(SchedulingConfigEntity.class))).thenReturn(entity);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);

        final StartupManager startupManager = new StartupManager(schedulingRepository, testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, null, Collections.emptyList(), null, systemStatusUtility);

        startupManager.initializeCronJobs();

        final String expectedLog = entity.toString();
        assertTrue(outputLogger.isLineContainingText(expectedLog));
    }

    @Test
    public void testValidateProviders() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, testGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        startupManager.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, testGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        testGlobalProperties.setBlackDuckUrl(null);
        startupManager.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Black Duck Provider..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Invalid; cause: Black Duck URL missing..."));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, testGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        startupManager.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Black Duck Provider..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testvalidateBlackDuckProviderHubWebserverEnvironmentSet() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(Mockito.mock(GlobalBlackDuckRepository.class), testAlertProperties);
        final TestBlackDuckProperties spiedGlobalProperties = Mockito.spy(testGlobalProperties);
        spiedGlobalProperties.setBlackDuckUrl("https://localhost:443");

        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, spiedGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        startupManager.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Black Duck Provider..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testValidateHubInvalidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, testGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        startupManager.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Black Duck Provider..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Invalid; cause:"));
    }

    @Test
    public void testValidateHubValidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);

        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final StartupManager startupManager = new StartupManager(null, testAlertProperties, testGlobalProperties, null, null, null, null, null, null, null, systemStatusUtility);
        startupManager.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Black Duck Provider..."));
        assertTrue(outputLogger.isLineContainingText("Black Duck Provider Valid!"));
    }
}
