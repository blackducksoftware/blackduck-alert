package com.synopsys.integration.alert.common.rest.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ProxyManagerTest {
    public static final String HOST = "host";
    public static final Integer PORT = 9999;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NON_PROXY_HOST = "nonProxyHostUrl";
    private ConfigurationModel configurationModel;
    private ProxyManager proxyManager;
    private SettingsUtility settingsUtility;

    @BeforeEach
    public void initTest() {
        settingsUtility = Mockito.mock(SettingsUtility.class);
        proxyManager = new ProxyManager(settingsUtility);
    }

    @Test
    public void testCreate() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModel()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        performAssertions(proxyInfo);
    }

    @Test
    public void testCreateNoProxy() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.empty());

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    @Test
    public void testCreateProxyInfoForHost() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModelWithNonProxyHosts()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(HOST);
        performAssertions(proxyInfo);
    }

    @Test
    public void testCreateProxyInfoForHostNoProxy() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.empty());

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(HOST);
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    @Test
    public void testCreateProxyInfoForHostNonProxyHost() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModelWithNonProxyHosts()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(NON_PROXY_HOST);
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    private SettingsProxyModel createSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setIsProxyPasswordSet(true);
        settingsProxyModel.setProxyPassword(PASSWORD);
        return settingsProxyModel;
    }

    private SettingsProxyModel createSettingsProxyModelWithNonProxyHosts() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();
        settingsProxyModel.setNonProxyHosts(List.of(NON_PROXY_HOST));
        return settingsProxyModel;
    }

    private void performAssertions(ProxyInfo proxyInfo) {
        assertTrue(proxyInfo.getHost().isPresent());
        assertTrue(proxyInfo.getUsername().isPresent());
        assertTrue(proxyInfo.getPassword().isPresent());

        assertEquals(HOST, proxyInfo.getHost().get());
        assertEquals(PORT, proxyInfo.getPort());
        assertEquals(USERNAME, proxyInfo.getUsername().get());
        assertEquals(PASSWORD, proxyInfo.getPassword().get());
    }

}
