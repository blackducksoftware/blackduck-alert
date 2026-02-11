/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.system.SystemInfoReader;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

class NotificationExtractorBlackDuckServicesFactoryCacheTest {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties properties = new AlertProperties();
    private final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
    private final SystemInfoReader systemInfoReader = new SystemInfoReader(gson);
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory = new BlackDuckPropertiesFactory(configurationModelConfigurationAccessor, gson, properties, proxyManager, systemInfoReader);

    private final Long blackDuckConfigId = 15L;

    private NotificationExtractorBlackDuckServicesFactoryCache cache;

    @BeforeEach
    public void init() {
        cache = new NotificationExtractorBlackDuckServicesFactoryCache(blackDuckPropertiesFactory);
    }

    @Test
    void retrieveBlackDuckServicesFactoryTest() throws AlertConfigurationException {
        ConfigurationModel configurationModel = createConfigurationModel();
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(blackDuckConfigId)).thenReturn(Optional.of(configurationModel));

        ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.any())).thenReturn(proxyInfo);

        //Create a BlackDuckServiceFactory and verify the same object is cached and returned.
        BlackDuckServicesFactory blackDuckServicesFactory = cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
        BlackDuckServicesFactory blackDuckServicesFactoryCached = cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);

        assertSame(blackDuckServicesFactory, blackDuckServicesFactoryCached, "Object reference of cached BlackDuckServiceFactory does not match. Verify the first factory is getting cached correctly.");
    }

    @Test
    void cacheClearedTest() throws AlertConfigurationException {
        ConfigurationModel configurationModel = createConfigurationModel();
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(blackDuckConfigId)).thenReturn(Optional.of(configurationModel));

        ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.any())).thenReturn(proxyInfo);

        BlackDuckServicesFactory blackDuckServicesFactory = cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
        cache.clear();
        BlackDuckServicesFactory blackDuckServicesFactoryCached = cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);

        assertNotSame(blackDuckServicesFactory, blackDuckServicesFactoryCached, "Object reference of cached BlackDuckServiceFactory matches. Verify that the first factory is getting cleared correctly.");
    }

    @Test
    void missingConfigurationTest() {
        try {
            cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
            fail("Expected AlertConfigurationException to be thrown due to missing optionalProperties.");
        } catch (AlertConfigurationException e) {
            //Pass
        }
    }

    @Test
    void invalidConfigurationTest() {
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 2L, "createdAt", "lastUpdated", ConfigContextEnum.GLOBAL, Map.of());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(blackDuckConfigId)).thenReturn(Optional.of(configurationModel));

        try {
            cache.retrieveBlackDuckServicesFactory(blackDuckConfigId);
            fail("Expected AlertConfigurationException to be thrown due to missing configuredFields in the ConfigurationModel.");
        } catch (AlertConfigurationException e) {
            //Pass
        }
    }

    private ConfigurationModel createConfigurationModel() {
        Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();

        ConfigurationFieldModel apiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        apiKey.setFieldValue("blackDuckApiKey");
        configuredFields.put(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, apiKey);

        ConfigurationFieldModel url = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        url.setFieldValue("http://blackDuckUrl");
        configuredFields.put(BlackDuckDescriptor.KEY_BLACKDUCK_URL, url);

        return new ConfigurationModel(1L, 2L, "createdAt", "lastUpdated", ConfigContextEnum.GLOBAL, configuredFields);
    }
}
