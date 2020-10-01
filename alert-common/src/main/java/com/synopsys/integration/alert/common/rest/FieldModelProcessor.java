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
package com.synopsys.integration.alert.common.rest;

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
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
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
    private final FieldValidationUtility fieldValidationAction;
    private final DescriptorProcessor descriptorProcessor;

    @Autowired
    public FieldModelProcessor(ConfigurationFieldModelConverter fieldModelConverter, FieldValidationUtility fieldValidationAction, DescriptorProcessor descriptorProcessor) {
        this.fieldModelConverter = fieldModelConverter;
        this.fieldValidationAction = fieldValidationAction;
        this.descriptorProcessor = descriptorProcessor;
    }

    public FieldModel performAfterReadAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterGetAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeDeleteAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeDeleteAction(fieldModel);
        }
        return fieldModel;
    }

    public void performAfterDeleteAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel.getDescriptorName(), fieldModel.getContext());
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            apiAction.afterDeleteAction(fieldModel);
        }
    }

    public FieldModel performBeforeSaveAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeSaveAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterSaveAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterSaveAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performBeforeUpdateAction(FieldModel fieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.beforeUpdateAction(fieldModel);
        }
        return fieldModel;
    }

    public FieldModel performAfterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(currentFieldModel);
        if (optionalApiAction.isPresent()) {
            ApiAction apiAction = optionalApiAction.get();
            return apiAction.afterUpdateAction(previousFieldModel, currentFieldModel);
        }
        return currentFieldModel;
    }

    public List<AlertFieldStatus> validateFieldModel(FieldModel fieldModel) {
        List<ConfigField> fields = descriptorProcessor.retrieveUIConfigFields(fieldModel.getContext(), fieldModel.getDescriptorName());
        Map<String, ConfigField> configFields = DataStructureUtils.mapToValues(fields, ConfigField::getKey);
        return fieldValidationAction.validateConfig(configFields, fieldModel);
    }

    public Collection<ConfigurationFieldModel> fillFieldModelWithExistingData(Long id, FieldModel fieldModel) throws AlertException {
        Optional<ConfigurationModel> configurationModel = descriptorProcessor.getSavedEntity(id);
        if (configurationModel.isPresent()) {
            Map<String, FieldValueModel> updatedFieldValueModels = updateConfigurationWithSavedConfiguration(fieldModel.getKeyToValues(), configurationModel.get().getCopyOfFieldList());
            fieldModel.setKeyToValues(updatedFieldValueModels);
            return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
        }

        return fieldModelConverter.convertToConfigurationFieldModelMap(fieldModel).values();
    }

    public FieldModel createCustomMessageFieldModel(FieldModel fieldModel) throws AlertException {
        String id = fieldModel.getId();
        FieldModel upToDateFieldModel = fieldModel;
        if (StringUtils.isNotBlank(id)) {
            Long convertedId = Long.parseLong(id);
            upToDateFieldModel = populateTestFieldModel(convertedId, fieldModel);
        }
        return upToDateFieldModel;
    }

    private FieldModel populateTestFieldModel(Long id, FieldModel fieldModel) throws AlertException {
        Collection<ConfigurationFieldModel> configurationFieldModels = fillFieldModelWithExistingData(id, fieldModel);
        Map<String, FieldValueModel> fields = new HashMap<>();
        for (ConfigurationFieldModel configurationFieldModel : configurationFieldModels) {
            FieldValueModel fieldValueModel = new FieldValueModel(configurationFieldModel.getFieldValues(), configurationFieldModel.isSet());
            fields.put(configurationFieldModel.getFieldKey(), fieldValueModel);
        }
        FieldModel newFieldModel = new FieldModel("", "", fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), fields);
        return fieldModel.fill(newFieldModel);
    }

    private Map<String, FieldValueModel> updateConfigurationWithSavedConfiguration(Map<String, FieldValueModel> newConfiguration, Collection<ConfigurationFieldModel> savedConfiguration) {
        Collection<ConfigurationFieldModel> sensitiveFields = savedConfiguration.stream().filter(ConfigurationFieldModel::isSensitive).collect(Collectors.toSet());
        for (ConfigurationFieldModel sensitiveConfigurationFieldModel : sensitiveFields) {
            String key = sensitiveConfigurationFieldModel.getFieldKey();
            if (newConfiguration.containsKey(key)) {
                FieldValueModel sensitiveFieldValueModel = newConfiguration.get(key);
                if (sensitiveFieldValueModel.getIsSet() && !sensitiveFieldValueModel.hasValues()) {
                    FieldValueModel newFieldModel = newConfiguration.get(key);
                    newFieldModel.setValues(sensitiveConfigurationFieldModel.getFieldValues());
                }
            }
        }
        return newConfiguration;
    }

}
