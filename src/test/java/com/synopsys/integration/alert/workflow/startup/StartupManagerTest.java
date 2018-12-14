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
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
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
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final BaseDescriptorAccessor baseDescriptorAccessor = Mockito.mock(BaseDescriptorAccessor.class);

        final StartupManager startupManager = new StartupManager(testAlertProperties, null, dailyTask, onDemandTask, purgeTask, phoneHomeTask, null, Collections.emptyList(), systemStatusUtility, systemValidator, baseConfigurationAccessor,
                encryptionUtility, Collections.emptyList(), baseDescriptorAccessor);

        startupManager.initializeCronJobs();

        final String expectedLog = null; // FIXME entity.toString();
        assertTrue(outputLogger.isLineContainingText(expectedLog));
    }
}
