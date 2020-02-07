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
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
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

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final String value, final boolean isSet) {
        final boolean actualSetValue = isSet || StringUtils.isNotBlank(value);
        if (!actualSetValue) {
            return Optional.empty();
        }

        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValue(value));
        return configurationModel;
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final Collection<String> values, final boolean isSet) {
        final boolean actualSetValue = isSet || (values != null && values.stream().anyMatch(StringUtils::isNotBlank));
        if (!actualSetValue) {
            return Optional.empty();
        }

        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValues(values));
        return configurationModel;
    }

    public final Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final ConfigContextEnum context = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        final String descriptorName = fieldModel.getDescriptorName();

        final List<DefinedFieldModel> fieldsForContext = descriptorAccessor.getFieldsForDescriptor(descriptorName, context);
        final Map<String, ConfigurationFieldModel> configurationModels = new HashMap<>();
        for (final DefinedFieldModel definedField : fieldsForContext) {
            fieldModel.getFieldValueModel(definedField.getKey())
                .flatMap(fieldValueModel -> convertFromDefinedFieldModel(definedField, fieldValueModel.getValues(), fieldValueModel.isSet()))
                .ifPresent(configurationFieldModel -> configurationModels.put(configurationFieldModel.getFieldKey(), configurationFieldModel));
        }

        return configurationModels;
    }

    public Map<String, FieldValueModel> convertToFieldValuesMap(final Collection<ConfigurationFieldModel> configurationFieldModels) {
        final Map<String, FieldValueModel> fields = new HashMap<>();
        for (final ConfigurationFieldModel fieldModel : configurationFieldModels) {
            final String key = fieldModel.getFieldKey();
            final Collection<String> values = fieldModel.getFieldValues();
            final FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
            fields.put(key, fieldValueModel);
        }
        return fields;
    }

    public FieldModel convertToFieldModel(final ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        final Long configId = configurationModel.getConfigurationId();
        final String descriptorName = getDescriptorName(configurationModel);
        final Map<String, FieldValueModel> fields = new HashMap<>();
        for (final ConfigurationFieldModel fieldModel : configurationModel.getCopyOfFieldList()) {
            populateAndSecureFields(fieldModel, fields);
        }

        return new FieldModel(configId.toString(), descriptorName, configurationModel.getDescriptorContext().name(), fields);
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

    private void populateAndSecureFields(final ConfigurationFieldModel fieldModel, final Map<String, FieldValueModel> fields) {
        final String key = fieldModel.getFieldKey();
        Collection<String> values = (!fieldModel.isSensitive()) ? fieldModel.getFieldValues() : List.of();
        final FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
        fields.put(key, fieldValueModel);
    }

    private String getDescriptorName(final ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        return descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId())
                   .map(RegisteredDescriptorModel::getName)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("Expected to find registered descriptor but none was found."));
    }

}
