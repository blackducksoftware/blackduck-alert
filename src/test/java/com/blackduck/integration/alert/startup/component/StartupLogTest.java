package com.blackduck.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.startup.component.ConfigurationLogger;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.OutputLogger;

class StartupLogTest {
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
    void testLogConfiguration() throws Exception {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "google.com", 3128);
        settingsProxyModel.setProxyUsername("AUser");
        settingsProxyModel.setIsProxyPasswordSet(true);
        settingsProxyModel.setProxyPassword("aPassword");

        SettingsUtility mockSettingsUtility = Mockito.mock(SettingsUtility.class);
        Mockito.when(mockSettingsUtility.getConfiguration()).thenReturn(Optional.of(settingsProxyModel));
        ProxyManager proxyManager = new ProxyManager(mockSettingsUtility);

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ConfigurationLogger configurationLogger = new ConfigurationLogger(proxyManager, testAlertProperties);

        configurationLogger.initializeComponent();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }
}
