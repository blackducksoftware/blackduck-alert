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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

@Component
public class ConfigurationFieldModelConverter {
    private final EncryptionUtility encryptionUtility;
    private final BaseDescriptorAccessor descriptorAccessor;

    @Autowired
    public ConfigurationFieldModelConverter(final EncryptionUtility encryptionUtility, final BaseDescriptorAccessor descriptorAccessor) {
        this.encryptionUtility = encryptionUtility;
        this.descriptorAccessor = descriptorAccessor;
    }

    public final FieldAccessor convertToFieldAccessor(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final Map<String, ConfigurationFieldModel> fields = convertToConfigurationFieldModelMap(fieldModel);
        return new FieldAccessor(fields);
    }

    public final Map<String, ConfigurationFieldModel> convertFromFieldModel(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        return convertToConfigurationFieldModelMap(fieldModel);
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final String value) {
        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValue(value));
        return configurationModel;
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(final DefinedFieldModel definedFieldModel, final Collection<String> values) {
        final Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValues(values));
        return configurationModel;
    }

    private Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(final FieldModel fieldModel) throws AlertDatabaseConstraintException {
        final ConfigContextEnum context = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        final String descriptorName = fieldModel.getDescriptorName();

        List<DefinedFieldModel> fieldsForContext = descriptorAccessor.getFieldsForDescriptor(descriptorName, context);
        List<DefinedFieldModel> definedFieldList = new LinkedList<>();
        definedFieldList.addAll(fieldsForContext);

        if (ConfigContextEnum.DISTRIBUTION == context) {
            final Optional<String> providerName = fieldModel.getFieldValue(CommonDistributionUIConfig.KEY_PROVIDER_NAME);
            // include all global field data as well if present in FieldModel
            definedFieldList.addAll(descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.GLOBAL));
            if (providerName.isPresent()) {
                definedFieldList.addAll(descriptorAccessor.getFieldsForDescriptor(providerName.get(), context));
            }
        }

        final Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        for (final DefinedFieldModel definedField : definedFieldList) {
            Collection<String> values = fieldModel.getFieldValues(definedField.getKey());
            convertFromDefinedFieldModel(definedField, values).ifPresent(configurationModels::add);
        }
        return configurationModels.stream().collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));
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
