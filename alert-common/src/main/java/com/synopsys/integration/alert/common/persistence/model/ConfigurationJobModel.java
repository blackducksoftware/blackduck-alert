/*
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
package com.synopsys.integration.alert.common.persistence.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ConfigurationJobModel extends AlertSerializableModel {
    private static final long serialVersionUID = 4714533679724412017L;
    private final UUID jobId;
    private final Set<ConfigurationModel> configurations;
    private final FieldUtility fieldUtility;

    public ConfigurationJobModel(UUID jobId, Set<ConfigurationModel> configurations) {
        this.jobId = jobId;
        this.configurations = configurations;
        this.fieldUtility = new FieldUtility(createKeyToFieldMap());
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<ConfigurationModel> getCopyOfConfigurations() {
        return Set.copyOf(configurations);
    }

    public FieldUtility getFieldUtility() {

        return fieldUtility;
    }

    public String getName() {
        return getFieldUtility().getStringOrNull(ChannelDistributionUIConfig.KEY_NAME);
    }

    public boolean isEnabled() {
        return getFieldUtility().getBoolean(ChannelDistributionUIConfig.KEY_ENABLED).orElse(Boolean.TRUE);
    }

    public String getChannelName() {
        return getFieldUtility().getStringOrNull(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
    }

    public String getProviderName() {
        return getFieldUtility().getStringOrNull(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
    }

    public String getProviderConfigId() {
        return getFieldUtility().getStringOrNull(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
    }

    public Long getProviderConfigIdAsLong() {
        return getFieldUtility().getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).orElse(null);
    }

    public FrequencyType getFrequencyType() {
        return getFieldUtility().getEnum(ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.class).orElse(null);
    }

    public ProcessingType getProcessingType() {
        return getFieldUtility().getEnum(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, ProcessingType.class).orElse(null);
    }

    public Set<String> getNotificationTypes() {
        return Set.copyOf(getFieldUtility().getAllStrings(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES));
    }

    private Map<String, ConfigurationFieldModel> createKeyToFieldMap() {
        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (ConfigurationModel config : configurations) {
            fieldMap.putAll(config.getCopyOfKeyToFieldMap());
        }
        return fieldMap;
    }

}
