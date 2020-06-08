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
