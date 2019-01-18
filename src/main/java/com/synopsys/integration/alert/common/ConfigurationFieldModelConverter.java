/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.web.model.FieldModel;

@Component
public class ConfigurationFieldModelConverter {

    public final FieldAccessor convertToFieldAccessor(final Map<String, ConfigField> configFieldMap, final FieldModel fieldModel) {
        final Map<String, ConfigurationFieldModel> fields = convertFromFieldModel(configFieldMap, fieldModel);
        return new FieldAccessor(fields);
    }

    public final Map<String, ConfigurationFieldModel> convertFromFieldModel(final Map<String, ConfigField> configFieldMap, final FieldModel fieldModel) {
        if (configFieldMap.isEmpty()) {
            return Map.of();
        }

        return convertToConfigurationFieldModelMap(configFieldMap, fieldModel);
    }

    private Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(final Map<String, ConfigField> configFieldMap, final FieldModel fieldModel) {
        return fieldModel.getKeyToValues()
                   .entrySet()
                   .stream()
                   .filter(entry -> configFieldMap.containsKey(entry.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey, entry -> createConfigurationFieldModel(configFieldMap.get(entry.getKey()), entry.getValue().getValues())));
    }

    private ConfigurationFieldModel createConfigurationFieldModel(final ConfigField configField, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel;
        final String key = configField.getKey();
        if (configField.isSensitive()) {
            configurationFieldModel = ConfigurationFieldModel.createSensitive(key);
        } else {
            configurationFieldModel = ConfigurationFieldModel.create(key);
        }
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }
}
