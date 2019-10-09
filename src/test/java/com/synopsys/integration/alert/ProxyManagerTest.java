package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.settings.DefaultSettingsUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ProxyManagerTest {
    public static final String HOST = "host";
    public static final String PORT = "9999";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private ConfigurationAccessor configurationAccessor;
    private ConfigurationModel configurationModel;
    private ProxyManager proxyManager;

    @BeforeEach
    public void initTest() throws Exception {
        configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        configurationModel = Mockito.mock(ConfigurationModel.class);

        SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));
        proxyManager = new ProxyManager(new DefaultSettingsUtility(new SettingsDescriptorKey(), configurationAccessor, null));
    }

    @Test
    public void testCreate() throws Exception {
        final ConfigurationFieldModel hostModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel portModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel usernameModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel passwordModel = Mockito.mock(ConfigurationFieldModel.class);

        Mockito.when(hostModel.getFieldValue()).thenReturn(Optional.of(HOST));
        Mockito.when(portModel.getFieldValue()).thenReturn(Optional.of(PORT));
        Mockito.when(usernameModel.getFieldValue()).thenReturn(Optional.of(USERNAME));
        Mockito.when(passwordModel.getFieldValue()).thenReturn(Optional.of(PASSWORD));

        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_HOST)).thenReturn(Optional.of(hostModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_PORT)).thenReturn(Optional.of(portModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_USERNAME)).thenReturn(Optional.of(usernameModel));
        Mockito.when(configurationModel.getField(SettingsDescriptor.KEY_PROXY_PWD)).thenReturn(Optional.of(passwordModel));

        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        assertEquals(HOST, proxyInfo.getHost().orElse(null));
        assertEquals(Integer.valueOf(PORT).intValue(), proxyInfo.getPort());
        assertEquals(USERNAME, proxyInfo.getUsername().orElse(null));
        assertEquals(PASSWORD, proxyInfo.getPassword().orElse(null));

    }

}
