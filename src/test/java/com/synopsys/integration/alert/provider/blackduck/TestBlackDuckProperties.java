/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.provider.blackduck;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class TestBlackDuckProperties extends BlackDuckProperties {
    private final MockAlertProperties testAlertProperties;
    private final TestProperties testProperties;
    private Integer blackDuckTimeout;
    private String blackDuckUrl;
    private boolean urlSet;

    private String blackDuckApiKey;
    private boolean apiKeySet;
    private Long configId;
    private boolean configIdSet;

    public static ProxyManager createMockedProxyManger() {
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        ProxyInfo proxyInfo = Mockito.mock(ProxyInfo.class);
        Mockito.when(proxyInfo.getHost()).thenReturn(Optional.empty());
        Mockito.when(proxyInfo.getPort()).thenReturn(-1);
        Mockito.when(proxyInfo.getUsername()).thenReturn(Optional.empty());
        Mockito.when(proxyInfo.getPassword()).thenReturn(Optional.empty());
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(proxyInfo);
        return proxyManager;
    }

    public TestBlackDuckProperties(MockAlertProperties alertProperties, ProxyManager proxyManager) {
        this(new Gson(), BlackDuckServicesFactory.createDefaultObjectMapper(), alertProperties, new TestProperties(), proxyManager);
    }

    public TestBlackDuckProperties(Gson gson, ObjectMapper objectMapper, MockAlertProperties alertProperties, TestProperties testProperties, ProxyManager proxyManager) {
        this(1L, gson, objectMapper, alertProperties, testProperties, proxyManager, 300, true);
    }

    public TestBlackDuckProperties(Long configId, Gson gson, ObjectMapper objectMapper, MockAlertProperties alertProperties, TestProperties testProperties, ProxyManager proxyManager, Integer blackDuckTimeout,
        boolean trustCertificates) {
        super(configId, gson, objectMapper, alertProperties, proxyManager, getConfigurationModel(testProperties));
        this.blackDuckTimeout = blackDuckTimeout;
        testAlertProperties = alertProperties;
        this.testProperties = testProperties;
        testAlertProperties.setAlertTrustCertificate(trustCertificates);
    }

    @Override
    public Optional<String> getBlackDuckUrl() {
        if (urlSet) {
            return Optional.ofNullable(blackDuckUrl);
        }
        return Optional.of(testProperties.getBlackDuckURL());
    }

    public void setBlackDuckUrl(String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
        urlSet = true;
    }

    @Override
    public String getApiToken() {
        if (apiKeySet) {
            return blackDuckApiKey;
        }
        return testProperties.getBlackDuckAPIToken();
    }

    public void setBlackDuckApiKey(String blackDuckApiKey) {
        this.blackDuckApiKey = blackDuckApiKey;
        apiKeySet = true;
    }

    @Override
    public Long getConfigId() {
        if (configIdSet) {
            return this.configId;
        }
        return super.getConfigId();
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
        this.configIdSet = true;
    }

    @Override
    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public void setBlackDuckTimeout(Integer blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    @Override
    public BlackDuckHttpClient createBlackDuckHttpClient(IntLogger intLogger) throws AlertException {
        testAlertProperties.setAlertTrustCertificate(true);
        return super.createBlackDuckHttpClient(intLogger);
    }

    public static ConfigurationModel getConfigurationModel(TestProperties testProperties) {
        String url = Optional.ofNullable(testProperties.getBlackDuckURL()).orElse("URL not set");
        String apiToken = testProperties.getBlackDuckAPIToken();
        String timeout = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT);

        final Long defaultDescriptorId = 1L;
        final Long defaultConfigurationId = 1L;
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);

        Mockito.when(configurationModel.getDescriptorId()).thenReturn(defaultDescriptorId);
        Mockito.when(configurationModel.getConfigurationId()).thenReturn(defaultConfigurationId);

        final String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(String.valueOf(timeout));
        Mockito.when(configurationModel.getField(blackDuckTimeoutKey)).thenReturn(Optional.of(blackDuckTimeoutField));

        final String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(apiToken);
        Mockito.when(configurationModel.getField(blackDuckApiKey)).thenReturn(Optional.of(blackDuckApiField));

        final String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(url);
        Mockito.when(configurationModel.getField(blackDuckProviderUrlKey)).thenReturn(Optional.of(blackDuckProviderUrlField));
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(Map.of(blackDuckApiKey, blackDuckApiField, blackDuckProviderUrlKey, blackDuckProviderUrlField, blackDuckTimeoutKey, blackDuckTimeoutField));
        Mockito.when(configurationModel.getCopyOfFieldList()).thenReturn(List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));

        return configurationModel;
    }

    @Override
    public BlackDuckServerConfig createBlackDuckServerConfig(IntLogger logger, int blackDuckTimeout, String blackDuckUsername, String blackDuckPassword) throws AlertException {
        return createHubServerConfigWithCredentials(logger);
    }

    public BlackDuckServerConfig createHubServerConfigWithCredentials(IntLogger logger) throws NumberFormatException, AlertException {
        return super.createBlackDuckServerConfig(logger, Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME),
            testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

}
