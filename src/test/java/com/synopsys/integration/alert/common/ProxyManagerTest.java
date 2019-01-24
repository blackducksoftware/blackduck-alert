package com.synopsys.integration.alert.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ProxyManagerTest {
    public static final String HOST = "host";
    public static final String PORT = "9999";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private BaseConfigurationAccessor configurationAccessor;
    private ConfigurationModel configurationModel;
    private ProxyManager proxyManager;

    @BeforeEach
    public void initTest() throws Exception {
        configurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));
        proxyManager = new ProxyManager(configurationAccessor);
    }

    @Test
    public void testCreate() throws Exception {
        ConfigurationFieldModel hostModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel portModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel usernameModel = Mockito.mock(ConfigurationFieldModel.class);
        ConfigurationFieldModel passwordModel = Mockito.mock(ConfigurationFieldModel.class);

        Mockito.when(hostModel.getFieldValue()).thenReturn(Optional.of(HOST));
        Mockito.when(portModel.getFieldValue()).thenReturn(Optional.of(PORT));
        Mockito.when(usernameModel.getFieldValue()).thenReturn(Optional.of(USERNAME));
        Mockito.when(passwordModel.getFieldValue()).thenReturn(Optional.of(PASSWORD));

        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_HOST)).thenReturn(Optional.of(hostModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_PORT)).thenReturn(Optional.of(portModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_USERNAME)).thenReturn(Optional.of(usernameModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_PASSWORD)).thenReturn(Optional.of(passwordModel));

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        assertEquals(HOST, proxyInfo.getHost().orElse(null));
        assertEquals(Integer.valueOf(PORT).intValue(), proxyInfo.getPort());
        assertEquals(USERNAME, proxyInfo.getUsername().orElse(null));
        assertEquals(PASSWORD, proxyInfo.getPassword().orElse(null));

    }

    @Test
    public void testCreateThrowException() {
        try {
            Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL)).thenThrow(new AlertDatabaseConstraintException("test error finding configuration"));
            proxyManager = new ProxyManager(configurationAccessor);
            proxyManager.createProxyInfo();
            Assertions.fail();
        } catch (AlertDatabaseConstraintException ex) {

        }
    }

    @Test
    public void testCreateThrowRuntimeException() throws Exception {
        try {
            Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL)).thenReturn(List.of());
            proxyManager = new ProxyManager(configurationAccessor);
            proxyManager.createProxyInfo();
            Assertions.fail();
        } catch (AlertRuntimeException ex) {

        }
    }
}
