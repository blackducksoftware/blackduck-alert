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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

@Component
public class ConfigurationFieldModelConverter {
    private final EncryptionUtility encryptionUtility;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final BaseConfigurationAccessor baseConfigurationAccessor;

    @Autowired
    public ConfigurationFieldModelConverter(final EncryptionUtility encryptionUtility, final BaseDescriptorAccessor descriptorAccessor, final BaseConfigurationAccessor baseConfigurationAccessor) {
        this.encryptionUtility = encryptionUtility;
        this.descriptorAccessor = descriptorAccessor;
        this.baseConfigurationAccessor = baseConfigurationAccessor;
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

        final List<DefinedFieldModel> fieldsForContext = descriptorAccessor.getFieldsForDescriptor(descriptorName, context);
        final List<DefinedFieldModel> definedFieldList = new LinkedList<>();
        definedFieldList.addAll(fieldsForContext);

        if (ConfigContextEnum.DISTRIBUTION == context) {
            final Optional<String> providerName = fieldModel.getFieldValue(CommonDistributionUIConfig.KEY_PROVIDER_NAME);
            // include all global field data as well if present in FieldModel
            definedFieldList.addAll(descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.GLOBAL));
            if (providerName.isPresent()) {
                definedFieldList.addAll(descriptorAccessor.getFieldsForDescriptor(providerName.get(), context));
            }
        }
        final Optional<ConfigurationModel> savedConfiguration;
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            final Long id = Long.parseLong(fieldModel.getId());
            savedConfiguration = baseConfigurationAccessor.getConfigurationById(id);
        } else {
            savedConfiguration = Optional.empty();
        }

        final Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        for (final DefinedFieldModel definedField : definedFieldList) {
            final Optional<FieldValueModel> currentField = fieldModel.getField(definedField.getKey());
            currentField.ifPresentOrElse(field -> {
                final Collection<String> values = field.getValues();
                if (areValuesEmptyOrBlank(values)) {
                    savedConfiguration.ifPresent(configurationModel -> {
                        configurationModel.getField(definedField.getKey()).ifPresent(configurationModels::add);
                    });
                } else {
                    convertFromDefinedFieldModel(definedField, values).ifPresent(configurationModels::add);
                }
            }, () -> {
                convertFromDefinedFieldModel(definedField, List.of()).ifPresent(configurationModels::add);
            });
        }

        //TODO for sensitive fields we need to get the values and update the ConfigurationFieldModels with the stored value IFF the fieldModel isSet = true and has no value

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

    private boolean areValuesEmptyOrBlank(final Collection<String> values) {
        if (null == values || values.isEmpty()) {
            return true;
        }
        final Optional<String> first = values.stream().filter(StringUtils::isNotBlank).findFirst();
        return first.isEmpty();
    }

}
