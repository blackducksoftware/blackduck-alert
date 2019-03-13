/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;

@Component
public class ConfigurationFieldModelConverter {
    private final EncryptionUtility encryptionUtility;
    private final DescriptorAccessor descriptorAccessor;

    @Autowired
    public ConfigurationFieldModelConverter(final EncryptionUtility encryptionUtility, final DescriptorAccessor descriptorAccessor) {
        this.encryptionUtility = encryptionUtility;
        this.descriptorAccessor = descriptorAccessor;
    }

    public final FieldAccessor convertToFieldAccessor(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final Map<String, ConfigurationFieldModel> fields = convertToConfigurationFieldModelMap(fieldModel);
        return new FieldAccessor(fields);
    }

    // TODO verify if we need this method or if we can just use the method this calls
    public final Map<String, ConfigurationFieldModel> convertFromFieldModel(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        return convertToConfigurationFieldModelMap(fieldModel);
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final String value, final boolean isSet) {
        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        final boolean actualSetValue = isSet || StringUtils.isNotBlank(value);
        configurationModel.ifPresent(model -> {
            model.setFieldValue(value);
            model.setSet(actualSetValue);
        });
        return configurationModel;
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final Collection<String> values, final boolean isSet) {
        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        final boolean actualSetValue = isSet || (values != null && values.stream().anyMatch(StringUtils::isNotBlank));
        configurationModel.ifPresent(model -> {
            model.setFieldValues(values);
            model.setSet(actualSetValue);
        });
        return configurationModel;
    }

    public final Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final ConfigContextEnum context = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        final String descriptorName = fieldModel.getDescriptorName();

        final List<DefinedFieldModel> fieldsForContext = descriptorAccessor.getFieldsForDescriptor(descriptorName, context);
        final Map<String, ConfigurationFieldModel> configurationModels = new HashMap<>();
        for (final DefinedFieldModel definedField : fieldsForContext) {
            fieldModel.getField(definedField.getKey())
                .flatMap(fieldValueModel -> convertFromDefinedFieldModel(definedField, fieldValueModel.getValues(), fieldValueModel.isSet()))
                .ifPresent(configurationFieldModel -> configurationModels.put(configurationFieldModel.getFieldKey(), configurationFieldModel));
        }

        return configurationModels;
    }

    private Optional<ConfigurationFieldModel> createEmptyModel(final DefinedFieldModel definedFieldModel) {
        ConfigurationFieldModel configurationModel = ConfigurationFieldModel.create(definedFieldModel.getKey());
        if (definedFieldModel.getSensitive()) {
            if (!encryptionUtility.isInitialized()) {
                return Optional.empty();
            }
            configurationModel = ConfigurationFieldModel.createSensitive(definedFieldModel.getKey());
        }
        return Optional.of(configurationModel);
    }

}
