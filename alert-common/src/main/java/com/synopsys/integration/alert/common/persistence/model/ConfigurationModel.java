/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;

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
