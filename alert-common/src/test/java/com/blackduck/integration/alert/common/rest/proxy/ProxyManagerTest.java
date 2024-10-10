/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.rest.proxy.ProxyInfo;

class ProxyManagerTest {
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
    void testCreate() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModel()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        performAssertions(proxyInfo);
    }

    @Test
    void testCreateNoProxy() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.empty());

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    @Test
    void testCreateProxyInfoForHost() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModelWithNonProxyHosts()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(HOST);
        performAssertions(proxyInfo);
    }

    @Test
    void testCreateProxyInfoForHostNoProxy() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.empty());

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(HOST);
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    @Test
    void testCreateProxyInfoForHostNonProxyHost() {
        Mockito.when(settingsUtility.getConfiguration()).thenReturn(Optional.of(createSettingsProxyModelWithNonProxyHosts()));

        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(NON_PROXY_HOST);
        assertTrue(proxyInfo.getHost().isEmpty());
        assertEquals(0, proxyInfo.getPort());
        assertTrue(proxyInfo.getUsername().isEmpty());
        assertTrue(proxyInfo.getPassword().isEmpty());
    }

    private SettingsProxyModel createSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, HOST, PORT);
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
