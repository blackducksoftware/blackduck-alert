package com.synopsys.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.OutputLogger;

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
        SettingsUtility mockSettingsUtility = createMockSettingsUtility();
        ProxyManager proxyManager = new ProxyManager(mockSettingsUtility);

        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ConfigurationLogger configurationLogger = new ConfigurationLogger(proxyManager, testAlertProperties);

        configurationLogger.initializeComponent();
        assertTrue(outputLogger.isLineContainingText("Alert Proxy Authenticated: true"));
    }

    private SettingsUtility createMockSettingsUtility() throws AlertException {
        SettingsUtility settingsUtility = Mockito.mock(SettingsUtility.class);

        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(0L, 0L, "", null, ConfigContextEnum.GLOBAL);

        ConfigurationFieldModel host = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_HOST);
        host.setFieldValue("google.com");
        configurationModel.put(host);

        ConfigurationFieldModel port = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_PORT);
        port.setFieldValue("3218");
        configurationModel.put(port);

        ConfigurationFieldModel username = ConfigurationFieldModel.create(ProxyManager.KEY_PROXY_USERNAME);
        username.setFieldValue("AUser");
        configurationModel.put(username);

        ConfigurationFieldModel password = ConfigurationFieldModel.createSensitive(ProxyManager.KEY_PROXY_PWD);
        password.setFieldValue("aPassword");
        configurationModel.put(password);

        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(configurationModel));

        return settingsUtility;
    }

}
