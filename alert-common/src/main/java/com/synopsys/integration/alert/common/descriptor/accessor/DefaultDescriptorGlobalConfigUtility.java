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
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public class DefaultDescriptorGlobalConfigUtility {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationAccessor configurationAccessor;
    private final ApiAction apiAction;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private DescriptorKey key;
    private ConfigContextEnum context;

    public DefaultDescriptorGlobalConfigUtility(DescriptorKey descriptorKey, ConfigurationAccessor configurationAccessor, ApiAction apiAction,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.key = descriptorKey;
        this.context = ConfigContextEnum.GLOBAL;
        this.configurationAccessor = configurationAccessor;
        this.apiAction = apiAction;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    public DescriptorKey getKey() {
        return key;
    }

    public boolean doesConfigurationExist() {
        try {
            return !configurationAccessor.getConfigurationsByDescriptorKeyAndContext(key, context).isEmpty();
        } catch (AlertException ex) {
            logger.debug("Error reading configuration from database.", ex);
            return false;
        }
    }

    public Optional<ConfigurationModel> getConfiguration() throws AlertException {
        return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(key, context)
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
        ConfigurationModel configuration = configurationAccessor.createConfiguration(key, context, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configuration);
        return apiAction.afterSaveAction(convertedFieldModel);
    }

    public FieldModel update(Long id, FieldModel fieldModel) throws AlertException {
        FieldModel beforeUpdateAction = apiAction.beforeUpdateAction(fieldModel);
        Map<String, ConfigurationFieldModel> valueMap = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeUpdateAction);
        Optional<ConfigurationModel> existingConfig = configurationAccessor.getConfigurationById(id);
        ConfigurationModel configurationModel;
        if (existingConfig.isPresent()) {
            Map<String, ConfigurationFieldModel> updatedValues = updateSensitiveFields(valueMap, existingConfig.get());
            configurationModel = configurationAccessor.updateConfiguration(id, updatedValues.values());
        } else {
            configurationModel = configurationAccessor.createConfiguration(key, context, valueMap.values());
        }
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
        return apiAction.afterUpdateAction(convertedFieldModel);
    }

    // TODO build a new utility to perform this action or try to refactor FieldModelProcessor into the alert-common sub-project
    private Map<String, ConfigurationFieldModel> updateSensitiveFields(Map<String, ConfigurationFieldModel> values, ConfigurationModel existingConfig) {
        Collection<ConfigurationFieldModel> sensitiveFields = existingConfig.getCopyOfFieldList().stream()
                                                                  .filter(ConfigurationFieldModel::isSensitive)
                                                                  .collect(Collectors.toSet());
        for (ConfigurationFieldModel sensitiveConfigurationFieldModel : sensitiveFields) {
            String key = sensitiveConfigurationFieldModel.getFieldKey();
            if (values.containsKey(key)) {
                ConfigurationFieldModel sensitiveFieldModel = values.get(key);
                if (!sensitiveFieldModel.isSet()) {
                    ConfigurationFieldModel newFieldModel = values.get(key);
                    newFieldModel.setFieldValues(sensitiveConfigurationFieldModel.getFieldValues());
                }
            }
        }
        return values;
    }
}
