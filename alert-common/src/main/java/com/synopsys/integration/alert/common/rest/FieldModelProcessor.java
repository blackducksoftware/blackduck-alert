/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Deprecated(forRemoval = true)
@Component
public class FieldModelProcessor {
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final DescriptorProcessor descriptorProcessor;

    @Autowired
    public FieldModelProcessor(ConfigurationFieldModelConverter fieldModelConverter, DescriptorProcessor descriptorProcessor) {
        this.fieldModelConverter = fieldModelConverter;
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
        Optional<ApiAction> optionalApiAction = descriptorProcessor.retrieveApiAction(fieldModel);
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
