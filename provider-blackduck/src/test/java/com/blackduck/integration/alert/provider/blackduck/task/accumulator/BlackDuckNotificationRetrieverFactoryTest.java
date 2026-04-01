/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.system.SystemInfoReader;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class BlackDuckNotificationRetrieverFactoryTest {
    private final String BLACKDUCK_URL = "https://example.com";

    @Test
    void createValidNotificationRetriever() {
        BlackDuckNotificationRetrieverFactory blackDuckNotificationRetrieverFactory = new BlackDuckNotificationRetrieverFactory();

        BlackDuckProperties blackDuckProperties = createBlackDuckProperties(BLACKDUCK_URL);
        Optional<BlackDuckNotificationRetriever> blackDuckNotificationRetrieverOptional = blackDuckNotificationRetrieverFactory.createBlackDuckNotificationRetriever(blackDuckProperties);

        assertTrue(blackDuckNotificationRetrieverOptional.isPresent());
    }

    @Test
    void createInvalidNotificationRetriever() {
        BlackDuckNotificationRetrieverFactory blackDuckNotificationRetrieverFactory = new BlackDuckNotificationRetrieverFactory();

        BlackDuckProperties blackDuckProperties = createBlackDuckProperties(null);
        Optional<BlackDuckNotificationRetriever> blackDuckNotificationRetrieverOptional = blackDuckNotificationRetrieverFactory.createBlackDuckNotificationRetriever(blackDuckProperties);

        assertTrue(blackDuckNotificationRetrieverOptional.isEmpty());
    }

    private BlackDuckProperties createBlackDuckProperties(String blackDuckUrl) {
        ConfigurationModel configurationModel = createConfigurationModel(blackDuckUrl);
        ProxyManager proxyManager = new ProxyManager(new MockSettingsUtility());
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        SystemInfoReader systemInfoReader = new SystemInfoReader(gson);
        return new BlackDuckProperties(
            1L,
            gson,
            BlackDuckServicesFactory.createDefaultObjectMapper(),
            new MockAlertProperties(),
            proxyManager,
            configurationModel,
            systemInfoReader
        );
    }

    private ConfigurationModel createConfigurationModel(String blackDuckUrl) {
        Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        addConfigurationField(configuredFields, ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED, "true");
        addConfigurationField(configuredFields, BlackDuckDescriptor.KEY_BLACKDUCK_URL, blackDuckUrl);
        addConfigurationField(configuredFields, BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, "fakeKey");

        return new ConfigurationModel(
            1L,
            1L,
            "createdAt",
            "lastUpdated",
            ConfigContextEnum.GLOBAL,
            configuredFields
        );
    }

    private void addConfigurationField(Map<String, ConfigurationFieldModel> configuredFields, String key, String value) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValue(value);
        configuredFields.put(key, configurationFieldModel);
    }

    class MockSettingsUtility implements SettingsUtility {

        @Override
        public DescriptorKey getKey() {
            return null;
        }

        @Override
        public Optional<SettingsProxyModel> getConfiguration() {
            return Optional.empty();
        }
    }
}
