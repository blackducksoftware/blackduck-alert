/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class FieldModelProcessor {
    private final DescriptorAccessor descriptorAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final ContentConverter contentConverter;
    private final FieldValidationAction fieldValidationAction;
    private final List<ConfigurationAction> allConfigurationActions;

    @Autowired
    public FieldModelProcessor(final DescriptorAccessor descriptorAccessor, final ConfigurationAccessor configurationAccessor, final DescriptorMap descriptorMap,
        final ConfigurationFieldModelConverter fieldModelConverter, final ContentConverter contentConverter, final FieldValidationAction fieldValidationAction,
        final List<ConfigurationAction> allConfigurationActions) {
        this.descriptorAccessor = descriptorAccessor;
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
        this.fieldModelConverter = fieldModelConverter;
        this.contentConverter = contentConverter;
        this.fieldValidationAction = fieldValidationAction;
        this.allConfigurationActions = allConfigurationActions;
    }

    //TODO: revisit the API of this class because we use a mix of objects. FieldModel and ConfigurationModel here.  Is that correct.
    public FieldModel performAfterReadAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterGetAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeDeleteAction(final ConfigurationModel configurationModel) throws AlertException {
        final FieldModel fieldModel = convertToFieldModel(configurationModel);
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeDeleteAction(fieldModel);
        }
        return fieldModel;
    }

    public void performAfterDeleteAction(final String descriptorName, final String context) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(descriptorName, context);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            apiAction.afterDeleteAction(descriptorName, context);
        }
    }

    public FieldModel performBeforeSaveAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeSaveAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterSaveAction(final Long id, final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterSaveAction(id, fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeUpdateAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeUpdateAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterUpdateAction(final Long id, final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterUpdateAction(id, fieldModel);
        }
        return fieldModel;
    }

    public Optional<TestAction> retrieveTestAction(final FieldModel fieldModel) {
        return retrieveTestAction(fieldModel.getDescriptorName(), fieldModel.getContext());
    }

    public Optional<TestAction> retrieveTestAction(final String descriptorName, final String context) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getTestAction(descriptorContext));
    }

    public Map<String, String> validateFieldModel(final FieldModel fieldModel) {
        final Map<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFields = retrieveUIConfigFields(fieldModel.getContext(), fieldModel.getDescriptorName())
                                                          .stream()
                                                          .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        fieldValidationAction.validateConfig(configFields, fieldModel, fieldErrors);
        return fieldErrors;
    }

    public Collection<ConfigurationFieldModel> fillFieldModelWithExistingData(final Long id, final FieldModel fieldModel) throws AlertException {
        final Optional<ConfigurationModel> configurationModel = getSavedEntity(id);
        if (configurationModel.isPresent()) {
            final Map<String, FieldValueModel> updatedFieldValueModels = updateConfigurationWithSavedConfiguration(fieldModel.getKeyToValues(), configurationModel.get().getCopyOfFieldList());
            fieldModel.setKeyToValues(updatedFieldValueModels);
            return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
        }

        return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
    }

    public FieldModel createTestFieldModel(final FieldModel fieldModel) throws AlertException {
        final String id = fieldModel.getId();
        FieldModel upToDateFieldModel = fieldModel;
        if (StringUtils.isNotBlank(id)) {
            final Long convertedId = contentConverter.getLongValue(id);
            upToDateFieldModel = populateTestFieldModel(convertedId, fieldModel);
        }
        return upToDateFieldModel;
    }

    private FieldModel populateTestFieldModel(final Long id, final FieldModel fieldModel) throws AlertException {
        final Collection<ConfigurationFieldModel> configurationFieldModels = fillFieldModelWithExistingData(id, fieldModel);
        final Map<String, FieldValueModel> fields = new HashMap<>();
        for (final ConfigurationFieldModel configurationFieldModel : configurationFieldModels) {
            final FieldValueModel fieldValueModel = new FieldValueModel(configurationFieldModel.getFieldValues(), configurationFieldModel.isSet());
            fields.put(configurationFieldModel.getFieldKey(), fieldValueModel);
        }
        final FieldModel newFieldModel = new FieldModel("", "", fields);
        return fieldModel.fill(newFieldModel);
    }

    public Optional<Descriptor> retrieveDescriptor(final String descriptorName) {
        return descriptorMap.getDescriptor(descriptorName);
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

    public String getDescriptorName(final ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        return descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId())
                   .map(RegisteredDescriptorModel::getName)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("Expected to find registered descriptor but none was found."));
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

    private void populateAndSecureFields(final ConfigurationFieldModel fieldModel, final Map<String, FieldValueModel> fields) {
        final String key = fieldModel.getFieldKey();
        Collection<String> values = Collections.emptyList();
        if (!fieldModel.isSensitive()) {
            values = fieldModel.getFieldValues();
        }
        final FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
        fields.put(key, fieldValueModel);
    }

    private List<ConfigField> retrieveUIConfigFields(final String context, final String descriptorName) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveUIConfigFields(descriptorContext, descriptorName);
    }

    private List<ConfigField> retrieveUIConfigFields(final ConfigContextEnum context, final String descriptorName) {
        final Optional<Descriptor> optionalDescriptor = retrieveDescriptor(descriptorName);
        final List<ConfigField> fieldsToReturn = new LinkedList<>();
        if (optionalDescriptor.isPresent()) {
            final Descriptor descriptor = optionalDescriptor.get();
            final Optional<UIConfig> uiConfig = descriptor.getUIConfig(context);
            fieldsToReturn.addAll(uiConfig.map(UIConfig::createFields).orElse(List.of()));
        }
        return fieldsToReturn;
    }

    public Optional<ConfigurationModel> getSavedEntity(final Long id) throws AlertException {
        if (null != id) {
            return configurationAccessor.getConfigurationById(id);
        }
        return Optional.empty();
    }

    private Optional<ApiAction> retrieveApiAction(final FieldModel fieldModel) {
        return retrieveApiAction(fieldModel.getDescriptorName(), fieldModel.getContext());
    }

    private Optional<ApiAction> retrieveApiAction(final String descriptorName, final String context) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getApiAction(descriptorContext));
    }

    private Optional<ConfigurationAction> retrieveConfigurationAction(final String descriptorName) {
        return allConfigurationActions.stream()
                   .filter(configurationAction -> configurationAction.getDescriptorName().equals(descriptorName))
                   .findFirst();
    }

    private Map<String, FieldValueModel> updateConfigurationWithSavedConfiguration(final Map<String, FieldValueModel> newConfiguration, final Collection<ConfigurationFieldModel> savedConfiguration) {
        final Collection<ConfigurationFieldModel> sensitiveFields = savedConfiguration.stream().filter(ConfigurationFieldModel::isSensitive).collect(Collectors.toSet());
        for (final ConfigurationFieldModel sensitiveConfigurationFieldModel : sensitiveFields) {
            final String key = sensitiveConfigurationFieldModel.getFieldKey();
            if (newConfiguration.containsKey(key)) {
                final FieldValueModel sensitiveFieldValueModel = newConfiguration.get(key);
                if (sensitiveFieldValueModel.isSet() && !sensitiveFieldValueModel.hasValues()) {
                    final FieldValueModel newFieldModel = newConfiguration.get(key);
                    newFieldModel.setValues(sensitiveConfigurationFieldModel.getFieldValues());
                }
            }
        }
        return newConfiguration;
    }

}
