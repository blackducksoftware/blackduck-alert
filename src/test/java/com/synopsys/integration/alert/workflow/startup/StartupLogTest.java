package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.TestBlackDuckProperties;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.workflow.startup.component.ConfigurationLogger;

public class StartupLogTest {
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
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);

        Mockito.when(proxyManager.getProxyHost()).thenReturn(Optional.of("google.com"));
        Mockito.when(proxyManager.getProxyPort()).thenReturn(Optional.of("3218"));
        Mockito.when(proxyManager.getProxyUsername()).thenReturn(Optional.of("AUser"));
        Mockito.when(proxyManager.getProxyPassword()).thenReturn(Optional.of("aPassword"));

        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        testGlobalProperties.setBlackDuckUrl("Black Duck Url");
        testGlobalProperties.setBlackDuckApiKey("Black Duck API Token");
        final TestBlackDuckProperties mockTestGlobalProperties = Mockito.spy(testGlobalProperties);
        final ConfigurationLogger configurationLogger = new ConfigurationLogger(proxyManager, mockTestGlobalProperties, testAlertProperties);

        configurationLogger.initialize();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
        assertTrue(outputLogger.isLineContainingText("BlackDuck API Token:           **********"));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Timeout:             300"));
    }
}
