package com.synopsys.integration.alert.common.provider.state;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;

public class StatefulProvider {
    private ProviderKey key;
    private Long configId;
    private String configName;
    private boolean configEnabled;
    private List<ProviderTask> tasks;
    private ProviderProperties properties;
    private ProviderDistributionFilter distributionFilter;
    private ProviderMessageContentCollector messageContentCollector;

    public static StatefulProvider create(ProviderKey providerKey, ConfigurationModel configurationModel,
        List<ProviderTask> tasks, ProviderProperties properties, ProviderDistributionFilter distributionFilter, ProviderMessageContentCollector messageContentCollector) {
        FieldAccessor fieldAccessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());
        boolean configEnabled = fieldAccessor.getBooleanOrFalse(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        String configName = fieldAccessor.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse("UNKNOWN CONFIGURATION");

        return new StatefulProvider(providerKey, configurationModel.getConfigurationId(), configName, configEnabled, tasks, properties, distributionFilter, messageContentCollector);
    }

    private StatefulProvider(ProviderKey key, Long configId, String configName, boolean configEnabled,
        List<ProviderTask> tasks, ProviderProperties properties, ProviderDistributionFilter distributionFilter, ProviderMessageContentCollector messageContentCollector) {
        this.key = key;
        this.configId = configId;
        this.configName = configName;
        this.configEnabled = configEnabled;
        this.tasks = tasks;
        this.properties = properties;
        this.distributionFilter = distributionFilter;
        this.messageContentCollector = messageContentCollector;
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

    public ProviderDistributionFilter getDistributionFilter() {
        return distributionFilter;
    }

    public ProviderMessageContentCollector getMessageContentCollector() {
        return messageContentCollector;
    }

}
