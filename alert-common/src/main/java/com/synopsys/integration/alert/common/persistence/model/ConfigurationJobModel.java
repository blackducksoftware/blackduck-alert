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
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ConfigurationJobModel extends AlertSerializableModel {
    private final UUID jobId;
    private final Set<ConfigurationModel> configurations;
    private final FieldAccessor fieldAccessor;

    public ConfigurationJobModel(final UUID jobId, final Set<ConfigurationModel> configurations) {
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
        return getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_NAME).orElse(null);
    }

    public String getChannelName() {
        return getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse(null);
    }

    public String getProviderName() {
        return getFieldAccessor().getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse(null);
    }

    public FrequencyType getFrequencyType() {
        return getFieldAccessor().getEnum(ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.class).orElse(null);
    }

    public FormatType getFormatType() {
        return getFieldAccessor().getEnum(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.class).orElse(null);
    }

    public Set<String> getNotificationTypes() {
        return getFieldAccessor().getAllStrings(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES).stream().collect(Collectors.toSet());
    }

    // TODO find out if collisions are possible and how to avoid them
    private Map<String, ConfigurationFieldModel> createKeyToFieldMap() {
        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationModel config : configurations) {
            fieldMap.putAll(config.getCopyOfKeyToFieldMap());
        }
        return fieldMap;
    }
}
