package com.synopsys.integration.alert.common.rest.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ProxyManagerTest {
    public static final String HOST = "host";
    public static final String PORT = "9999";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private ConfigurationModel configurationModel;
    private ProxyManager proxyManager;
    private SettingsUtility settingsUtility;

    @BeforeEach
    public void initTest() {
        configurationModel = Mockito.mock(ConfigurationModel.class);

        settingsUtility = Mockito.mock(SettingsUtility.class);
        proxyManager = new ProxyManager(settingsUtility);
    }

    @Test
    public void testCreate() {
        ConfigurationFieldModel hostModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel portModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel usernameModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel passwordModel = Mockito.mock(ConfigurationFieldModel.class);

        Mockito.when(hostModel.getFieldValue()).thenReturn(Optional.of(HOST));
        Mockito.when(portModel.getFieldValue()).thenReturn(Optional.of(PORT));
        Mockito.when(usernameModel.getFieldValue()).thenReturn(Optional.of(USERNAME));
        Mockito.when(passwordModel.getFieldValue()).thenReturn(Optional.of(PASSWORD));

        Mockito.when(configurationModel.getField(ProxyManager.KEY_PROXY_HOST)).thenReturn(Optional.of(hostModel));
        Mockito.when(configurationModel.getField(ProxyManager.KEY_PROXY_PORT)).thenReturn(Optional.of(portModel));
        Mockito.when(configurationModel.getField(ProxyManager.KEY_PROXY_USERNAME)).thenReturn(Optional.of(usernameModel));
        Mockito.when(configurationModel.getField(ProxyManager.KEY_PROXY_PWD)).thenReturn(Optional.of(passwordModel));

        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(configurationModel));

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        assertEquals(HOST, proxyInfo.getHost().orElse(null));
        assertEquals(Integer.valueOf(PORT).intValue(), proxyInfo.getPort());
        assertEquals(USERNAME, proxyInfo.getUsername().orElse(null));
        assertEquals(PASSWORD, proxyInfo.getPassword().orElse(null));

    }

}
