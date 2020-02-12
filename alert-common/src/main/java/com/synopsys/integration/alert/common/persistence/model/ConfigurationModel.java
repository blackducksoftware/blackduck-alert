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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public final class ConfigurationModel extends AlertSerializableModel {
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
        descriptorId = registeredDescriptorId;
        configurationId = descriptorConfigId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.context = context;
        configuredFields = new HashMap<>();
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

    // TODO it might be worthwhile to expose a builder for this class rather than allowing this method to be used outside of the accessors
    public void put(ConfigurationFieldModel configFieldModel) {
        Objects.requireNonNull(configFieldModel);
        String fieldKey = configFieldModel.getFieldKey();
        Objects.requireNonNull(fieldKey);
        if (configuredFields.containsKey(fieldKey)) {
            ConfigurationFieldModel oldConfigField = configuredFields.get(fieldKey);
            List<String> values = combine(oldConfigField, configFieldModel);
            oldConfigField.setFieldValues(values);
        } else {
            configuredFields.put(fieldKey, configFieldModel);
        }
    }

    private List<String> combine(ConfigurationFieldModel first, ConfigurationFieldModel second) {
        return Stream.concat(first.getFieldValues().stream(), second.getFieldValues().stream()).collect(Collectors.toList());
    }
}
