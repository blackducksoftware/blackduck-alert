/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.state;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public class StatefulProvider {
    private final ProviderKey key;
    private final Long configId;
    private final String configName;
    private final boolean configEnabled;
    private final List<ProviderTask> tasks;
    private final ProviderProperties properties;
    private final ProviderDistributionFilter distributionFilter;
    private final ProviderMessageContentCollector messageContentCollector;

    public static StatefulProvider create(
        ProviderKey providerKey,
        ConfigurationModel configurationModel,
        List<ProviderTask> tasks,
        ProviderProperties properties,
        ProviderDistributionFilter distributionFilter,
        ProviderMessageContentCollector messageContentCollector
    ) {
        Map<String, ConfigurationFieldModel> keyToFieldMap = configurationModel.getCopyOfKeyToFieldMap();
        Boolean configEnabled = Optional.ofNullable(keyToFieldMap.get(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED))
                                    .flatMap(ConfigurationFieldModel::getFieldValue)
                                    .map(Boolean::valueOf)
                                    .orElse(false);
        String configName = Optional.ofNullable(keyToFieldMap.get(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                                .flatMap(ConfigurationFieldModel::getFieldValue)
                                .orElse(ProviderProperties.UNKNOWN_CONFIG_NAME);
        return new StatefulProvider(providerKey, configurationModel.getConfigurationId(), configName, configEnabled, tasks, properties, distributionFilter, messageContentCollector);
    }

    private StatefulProvider(
        ProviderKey key,
        Long configId,
        String configName,
        boolean configEnabled,
        List<ProviderTask> tasks,
        ProviderProperties properties,
        ProviderDistributionFilter distributionFilter,
        ProviderMessageContentCollector messageContentCollector
    ) {
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
