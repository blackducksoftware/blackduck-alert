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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ConfigurationModel extends AlertSerializableModel {
    private final Long descriptorId;
    private final Long configurationId;
    private final String createdAt;
    private final String lastUpdated;
    private final ConfigContextEnum context;
    private final Map<String, ConfigurationFieldModel> configuredFields;

    public ConfigurationModel(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, String context) {
        this(registeredDescriptorId, descriptorConfigId, createdAt, lastUpdated, ConfigContextEnum.valueOf(context));
    }

    public ConfigurationModel(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, ConfigContextEnum context) {
        this(registeredDescriptorId, descriptorConfigId, createdAt, lastUpdated, context, new HashMap<>());
    }

    public ConfigurationModel(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, ConfigContextEnum context, Map<String, ConfigurationFieldModel> configuredFields) {
        descriptorId = registeredDescriptorId;
        configurationId = descriptorConfigId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.context = context;
        this.configuredFields = configuredFields;
    }

    protected Map<String, ConfigurationFieldModel> getConfiguredFields() {
        return configuredFields;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public ConfigContextEnum getDescriptorContext() {
        return context;
    }

    public Optional<ConfigurationFieldModel> getField(String fieldKey) {
        Objects.requireNonNull(fieldKey);
        return Optional.ofNullable(configuredFields.get(fieldKey));
    }

    public List<ConfigurationFieldModel> getCopyOfFieldList() {
        return new ArrayList<>(configuredFields.values());
    }

    public Map<String, ConfigurationFieldModel> getCopyOfKeyToFieldMap() {
        return new HashMap<>(configuredFields);
    }

    public ConfigurationModelMutable createMutableCopy() {
        ConfigurationModelMutable mutableCopy = new ConfigurationModelMutable(descriptorId, configurationId, createdAt, lastUpdated, context);
        mutableCopy.getConfiguredFields().putAll(configuredFields);
        return mutableCopy;
    }

    public boolean isConfiguredFieldsEmpty() {
        return configuredFields == null || configuredFields.isEmpty();
    }

    public boolean isConfiguredFieldsNotEmpty() {
        return configuredFields != null && !configuredFields.isEmpty();
    }
}
