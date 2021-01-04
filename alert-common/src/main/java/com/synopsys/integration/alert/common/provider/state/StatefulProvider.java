/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.provider.state;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
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

    public static StatefulProvider create(ProviderKey providerKey, ConfigurationModel configurationModel,
        List<ProviderTask> tasks, ProviderProperties properties, ProviderDistributionFilter distributionFilter, ProviderMessageContentCollector messageContentCollector) {
        FieldUtility fieldUtility = new FieldUtility(configurationModel.getCopyOfKeyToFieldMap());
        boolean configEnabled = fieldUtility.getBooleanOrFalse(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        String configName = fieldUtility.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse(ProviderProperties.UNKNOWN_CONFIG_NAME);

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
