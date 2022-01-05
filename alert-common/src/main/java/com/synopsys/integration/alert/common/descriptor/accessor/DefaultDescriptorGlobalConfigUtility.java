/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DefaultDescriptorGlobalConfigUtility {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ApiAction apiAction;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private final DescriptorKey key;
    private final ConfigContextEnum context;

    public DefaultDescriptorGlobalConfigUtility(DescriptorKey descriptorKey, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, ApiAction apiAction,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.key = descriptorKey;
        this.context = ConfigContextEnum.GLOBAL;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.apiAction = apiAction;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    public DescriptorKey getKey() {
        return key;
    }

    public boolean doesConfigurationExist() {
        return !configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(key, context).isEmpty();
    }

    public Optional<ConfigurationModel> getConfiguration() {
        return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(key, context)
                   .stream()
                   .findFirst();
    }

    public Optional<FieldModel> getFieldModel() throws AlertException {
        Optional<ConfigurationModel> configurationModelOptional = getConfiguration();

        if (configurationModelOptional.isPresent()) {
            ConfigurationModel configurationModel = configurationModelOptional.get();
            FieldModel fieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
            return Optional.ofNullable(apiAction.afterGetAction(fieldModel));
        }

        return Optional.empty();
    }

    public FieldModel save(FieldModel fieldModel) throws AlertException {
        FieldModel beforeAction = apiAction.beforeSaveAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeAction).values();
        ConfigurationModel configuration = configurationModelConfigurationAccessor.createConfiguration(key, context, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configuration);
        return apiAction.afterSaveAction(convertedFieldModel);
    }

    public FieldModel update(Long id, FieldModel fieldModel) throws AlertException {
        FieldModel beforeUpdateActionFieldModel = apiAction.beforeUpdateAction(fieldModel);
        Map<String, ConfigurationFieldModel> valueMap = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeUpdateActionFieldModel);
        Optional<ConfigurationModel> existingConfig = configurationModelConfigurationAccessor.getConfigurationById(id);
        ConfigurationModel configurationModel;
        if (existingConfig.isPresent()) {
            Map<String, ConfigurationFieldModel> updatedValues = updateSensitiveFields(valueMap, existingConfig.get());
            configurationModel = configurationModelConfigurationAccessor.updateConfiguration(id, updatedValues.values());
        } else {
            configurationModel = configurationModelConfigurationAccessor.createConfiguration(key, context, valueMap.values());
        }
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
        return apiAction.afterUpdateAction(beforeUpdateActionFieldModel, convertedFieldModel);
    }

    // TODO build a new utility to perform this action or try to refactor FieldModelProcessor into the alert-common sub-project
    private Map<String, ConfigurationFieldModel> updateSensitiveFields(Map<String, ConfigurationFieldModel> values, ConfigurationModel existingConfig) {
        Collection<ConfigurationFieldModel> sensitiveFields = existingConfig.getCopyOfFieldList().stream()
                                                                  .filter(ConfigurationFieldModel::isSensitive)
                                                                  .collect(Collectors.toSet());
        for (ConfigurationFieldModel sensitiveConfigurationFieldModel : sensitiveFields) {
            String fieldKey = sensitiveConfigurationFieldModel.getFieldKey();
            if (values.containsKey(fieldKey)) {
                ConfigurationFieldModel sensitiveFieldModel = values.get(fieldKey);
                if (!sensitiveFieldModel.isSet()) {
                    ConfigurationFieldModel newFieldModel = values.get(fieldKey);
                    newFieldModel.setFieldValues(sensitiveConfigurationFieldModel.getFieldValues());
                }
            }
        }
        return values;
    }

}
