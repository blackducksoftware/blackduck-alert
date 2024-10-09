package com.blackduck.integration.alert.api.provider.state;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.api.provider.lifecycle.ProviderTask;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

public class StatefulProvider {
    private final ProviderKey key;
    private final Long configId;
    private final String configName;
    private final boolean configEnabled;
    private final List<ProviderTask> tasks;
    private final ProviderProperties properties;

    public static StatefulProvider create(
        ProviderKey providerKey,
        ConfigurationModel configurationModel,
        List<ProviderTask> tasks,
        ProviderProperties properties
    ) {
        Map<String, ConfigurationFieldModel> keyToFieldMap = configurationModel.getCopyOfKeyToFieldMap();
        Boolean configEnabled = Optional.ofNullable(keyToFieldMap.get(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED))
                                    .flatMap(ConfigurationFieldModel::getFieldValue)
                                    .map(Boolean::valueOf)
                                    .orElse(false);
        String configName = Optional.ofNullable(keyToFieldMap.get(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                                .flatMap(ConfigurationFieldModel::getFieldValue)
                                .orElse(ProviderProperties.UNKNOWN_CONFIG_NAME);
        return new StatefulProvider(providerKey, configurationModel.getConfigurationId(), configName, configEnabled, tasks, properties);
    }

    private StatefulProvider(
        ProviderKey key,
        Long configId,
        String configName,
        boolean configEnabled,
        List<ProviderTask> tasks,
        ProviderProperties properties
    ) {
        this.key = key;
        this.configId = configId;
        this.configName = configName;
        this.configEnabled = configEnabled;
        this.tasks = tasks;
        this.properties = properties;
    }

    public ProviderKey getKey() {
        return key;
    }

    public Long getConfigId() {
        return configId;
    }

    public String getConfigName() {
        return configName;
    }

    public boolean isConfigEnabled() {
        return configEnabled;
    }

    public List<ProviderTask> getTasks() {
        return tasks;
    }

    public ProviderProperties getProperties() {
        return properties;
    }

}
