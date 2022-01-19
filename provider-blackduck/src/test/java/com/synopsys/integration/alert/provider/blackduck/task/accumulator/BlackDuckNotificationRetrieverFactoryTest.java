package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

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
        return new BlackDuckProperties(
            1L,
            new Gson(),
            BlackDuckServicesFactory.createDefaultObjectMapper(),
            new MockAlertProperties(),
            proxyManager,
            configurationModel
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
