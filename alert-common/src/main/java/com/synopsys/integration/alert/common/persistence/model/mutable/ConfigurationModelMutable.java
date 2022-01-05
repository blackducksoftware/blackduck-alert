/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.mutable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public class ConfigurationModelMutable extends ConfigurationModel {
    public ConfigurationModelMutable(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, String context) {
        super(registeredDescriptorId, descriptorConfigId, createdAt, lastUpdated, context);
    }

    public ConfigurationModelMutable(Long registeredDescriptorId, Long descriptorConfigId, String createdAt, String lastUpdated, ConfigContextEnum context) {
        super(registeredDescriptorId, descriptorConfigId, createdAt, lastUpdated, context);
    }

    public void put(ConfigurationFieldModel configFieldModel) {
        Objects.requireNonNull(configFieldModel);
        String fieldKey = configFieldModel.getFieldKey();
        Objects.requireNonNull(fieldKey);
        if (getConfiguredFields().containsKey(fieldKey)) {
            ConfigurationFieldModel oldConfigField = getConfiguredFields().get(fieldKey);
            List<String> values = combine(oldConfigField, configFieldModel);
            oldConfigField.setFieldValues(values);
        } else {
            getConfiguredFields().put(fieldKey, configFieldModel);
        }
    }

    private List<String> combine(ConfigurationFieldModel first, ConfigurationFieldModel second) {
        return Stream.concat(first.getFieldValues().stream(), second.getFieldValues().stream()).collect(Collectors.toList());
    }
}
