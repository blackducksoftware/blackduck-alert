/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.persistence.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderGlobalUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ConfigurationJobModel extends AlertSerializableModel {
    private final UUID jobId;
    private final Set<ConfigurationModel> configurations;
    private final FieldAccessor fieldAccessor;

    public ConfigurationJobModel(UUID jobId, Set<ConfigurationModel> configurations) {
        this.jobId = jobId;
        this.configurations = configurations;
        this.fieldAccessor = new FieldAccessor(createKeyToFieldMap());
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<ConfigurationModel> getCopyOfConfigurations() {
        return Set.copyOf(configurations);
    }

    public FieldAccessor getFieldAccessor() {

        return fieldAccessor;
    }

    public String getName() {
        return getFieldAccessor().getStringOrNull(ChannelDistributionUIConfig.KEY_NAME);
    }

    public boolean isEnabled() {
        return getFieldAccessor().getBoolean(ChannelDistributionUIConfig.KEY_ENABLED).orElse(Boolean.TRUE);
    }

    public String getChannelName() {
        return getFieldAccessor().getStringOrNull(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
    }

    public String getProviderName() {
        return getFieldAccessor().getStringOrNull(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
    }

    public String getProviderConfigName() {
        return getFieldAccessor().getStringOrNull(ProviderGlobalUIConfig.KEY_PROVIDER_CONFIG_NAME);
    }

    public FrequencyType getFrequencyType() {
        return getFieldAccessor().getEnum(ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.class).orElse(null);
    }

    public FormatType getFormatType() {
        return getFieldAccessor().getEnum(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.class).orElse(null);
    }

    public Set<String> getNotificationTypes() {
        return Set.copyOf(getFieldAccessor().getAllStrings(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES));
    }

    private Map<String, ConfigurationFieldModel> createKeyToFieldMap() {
        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (ConfigurationModel config : configurations) {
            fieldMap.putAll(config.getCopyOfKeyToFieldMap());
        }
        return fieldMap;
    }
}
