/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;

@Component
public class FieldModelProcessor {
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final FieldValidationAction fieldValidationAction;
    private final DescriptorProcessor descriptorProcessor;

    @Autowired
    public FieldModelProcessor(final ConfigurationFieldModelConverter fieldModelConverter, final FieldValidationAction fieldValidationAction, final DescriptorProcessor descriptorProcessor) {
        this.fieldModelConverter = fieldModelConverter;
        this.fieldValidationAction = fieldValidationAction;
        this.descriptorProcessor = descriptorProcessor;
    }

    public FieldModel performAfterReadAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterGetAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeDeleteAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeDeleteAction(fieldModel);
        }
        return fieldModel;
    }

    public void performAfterDeleteAction(final String descriptorName, final String context) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(descriptorName, context);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            apiAction.afterDeleteAction(descriptorName, context);
        }
    }

    public FieldModel performBeforeSaveAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeSaveAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterSaveAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterSaveAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeUpdateAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeUpdateAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterUpdateAction(final FieldModel fieldModel) throws AlertException {
        final Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            final ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterUpdateAction(fieldModel);
        }
        return fieldModel;
    }

    public Map<String, String> validateFieldModel(final FieldModel fieldModel) {
        final Map<String, String> fieldErrors = new HashMap<>();
        final List<ConfigField> fields = descriptorProcessor.retrieveUIConfigFields(fieldModel.getContext(), fieldModel.getDescriptorName());
        Map<String, ConfigField> configFields = DataStructureUtils.mapToValues(fields, ConfigField::getKey);
        fieldValidationAction.validateConfig(configFields, fieldModel, fieldErrors);
        return fieldErrors;
    }

    public Collection<ConfigurationFieldModel> fillFieldModelWithExistingData(final Long id, final FieldModel fieldModel) throws AlertException {
        final Optional<ConfigurationModel> configurationModel = descriptorProcessor.getSavedEntity(id);
        if (configurationModel.isPresent()) {
            final Map<String, FieldValueModel> updatedFieldValueModels = updateConfigurationWithSavedConfiguration(fieldModel.getKeyToValues(), configurationModel.get().getCopyOfFieldList());
            fieldModel.setKeyToValues(updatedFieldValueModels);
            return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
        }

        return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
    }

    public FieldModel createCustomMessageFieldModel(final FieldModel fieldModel) throws AlertException {
        final String id = fieldModel.getId();
        FieldModel upToDateFieldModel = fieldModel;
        if (StringUtils.isNotBlank(id)) {
            final Long convertedId = Long.parseLong(id);
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
        final FieldModel newFieldModel = new FieldModel("", "", fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), fields);
        return fieldModel.fill(newFieldModel);
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
