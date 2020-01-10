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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.web.config.FieldModelProcessor;

@Component
@Order(20)
// TODO Remove this class in 6.0.0
// TODO Revisit the order of the startup components
public class AuthenticationSettingsMigration extends StartupComponent {
    private Logger logger = LoggerFactory.getLogger(AuthenticationSettingsMigration.class);
    private EnvironmentVariableUtility environmentUtility;
    private DescriptorAccessor descriptorAccessor;
    private ConfigurationAccessor fieldConfigurationAccessor;
    private ConfigurationFieldModelConverter modelConverter;
    private FieldModelProcessor fieldModelProcessor;
    private AuthenticationDescriptorKey authenticationDescriptorKey;
    private SettingsUtility settingsUtility;

    @Autowired
    public AuthenticationSettingsMigration(EnvironmentVariableUtility environmentUtility, DescriptorAccessor descriptorAccessor, ConfigurationAccessor fieldConfigurationAccessor,
        ConfigurationFieldModelConverter modelConverter, FieldModelProcessor fieldModelProcessor, AuthenticationDescriptorKey authenticationDescriptorKey, SettingsUtility settingsUtility) {
        this.environmentUtility = environmentUtility;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationAccessor = fieldConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.fieldModelProcessor = fieldModelProcessor;
        this.authenticationDescriptorKey = authenticationDescriptorKey;
        this.settingsUtility = settingsUtility;
    }

    @Override
    protected void initialize() {
        logger.info("Settings authentication variable migration start...");
        try {
            List<DefinedFieldModel> fieldsForDescriptor = descriptorAccessor.getFieldsForDescriptor(authenticationDescriptorKey, ConfigContextEnum.GLOBAL).stream()
                                                              .sorted(Comparator.comparing(DefinedFieldModel::getKey))
                                                              .collect(Collectors.toList());
            List<ConfigurationModel> foundConfigurationModels = fieldConfigurationAccessor.getConfigurationByDescriptorKeyAndContext(authenticationDescriptorKey, ConfigContextEnum.GLOBAL);

            Map<String, ConfigurationFieldModel> existingConfiguredFields = new HashMap<>();
            foundConfigurationModels.forEach(config -> existingConfiguredFields.putAll(config.getCopyOfKeyToFieldMap()));

            Set<ConfigurationFieldModel> configurationModels = createFieldModelsFromDefinedFields(fieldsForDescriptor, existingConfiguredFields);
            boolean overwriteConfig = settingsUtility.getConfiguration()
                                          .flatMap(configurationModel -> configurationModel.getField(SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE))
                                          .flatMap(ConfigurationFieldModel::getFieldValue)
                                          .map(Boolean::valueOf)
                                          .orElse(Boolean.FALSE);
            updateConfigurationFields(overwriteConfig, foundConfigurationModels, configurationModels);
        } catch (AlertException ex) {
            logger.error("Error migrating old authentication settings to new configuration.", ex);
        }
        logger.info("Settings authentication variable migration finished...");
    }

    private Set<ConfigurationFieldModel> createFieldModelsFromDefinedFields(List<DefinedFieldModel> fieldsForDescriptor, Map<String, ConfigurationFieldModel> existingConfiguredFields) {
        Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        logger.info("  ### Environment Variables ### ");
        for (DefinedFieldModel fieldModel : fieldsForDescriptor) {
            String key = fieldModel.getKey();
            String convertedKey = environmentUtility.convertKeyToProperty(settingsUtility.getKey(), key);
            boolean hasEnvironmentValue = environmentUtility.hasEnvironmentValue(convertedKey);
            logger.info("    {}", convertedKey);
            logger.debug("         Environment Variable Found - {}", hasEnvironmentValue);
            if (existingConfiguredFields.containsKey(key)) {
                configurationModels.add(existingConfiguredFields.get(key));
            } else if (hasEnvironmentValue) {
                environmentUtility.getEnvironmentValue(convertedKey, null)
                    .flatMap(value -> modelConverter.convertFromDefinedFieldModel(fieldModel, value, StringUtils.isNotBlank(value)))
                    .ifPresent(configurationModels::add);
            }
        }
        return configurationModels;
    }

    private void updateConfigurationFields(boolean overwriteCurrentConfig, List<ConfigurationModel> foundConfigurationModels, Set<ConfigurationFieldModel> configurationModels)
        throws AlertException {
        Optional<ConfigurationModel> optionalFoundModel = foundConfigurationModels
                                                              .stream()
                                                              .findFirst()
                                                              .filter(model -> overwriteCurrentConfig);
        logger.info("  ### Processing Configuration ###");
        if (optionalFoundModel.isPresent()) {
            ConfigurationModel foundModel = optionalFoundModel.get();
            logger.info("    Overwriting values with environment.");
            Collection<ConfigurationFieldModel> updatedFields = updateAction(authenticationDescriptorKey, foundModel.getCreatedAt(), foundModel.getLastUpdated(), configurationModels);
            fieldConfigurationAccessor.updateConfiguration(foundModel.getConfigurationId(), updatedFields);
        } else if (foundConfigurationModels.isEmpty() && !configurationModels.isEmpty()) {
            logger.info("    Writing initial values from environment.");
            Collection<ConfigurationFieldModel> savedFields = saveAction(authenticationDescriptorKey, configurationModels);
            fieldConfigurationAccessor.createConfiguration(authenticationDescriptorKey, ConfigContextEnum.GLOBAL, savedFields);
        }
    }

    private Collection<ConfigurationFieldModel> updateAction(DescriptorKey descriptorKey, String createdAt, String lastUpdated, Collection<ConfigurationFieldModel> configurationFieldModels)
        throws AlertException {
        Map<String, FieldValueModel> fieldValueModelMap = modelConverter.convertToFieldValuesMap(configurationFieldModels);
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), createdAt, lastUpdated, fieldValueModelMap);
        FieldModel updatedFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
        return modelConverter.convertToConfigurationFieldModelMap(updatedFieldModel).values();
    }

    private Collection<ConfigurationFieldModel> saveAction(DescriptorKey descriptorKey, Collection<ConfigurationFieldModel> configurationFieldModels) throws AlertException {
        Map<String, FieldValueModel> fieldValueModelMap = modelConverter.convertToFieldValuesMap(configurationFieldModels);
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        FieldModel savedFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
        return modelConverter.convertToConfigurationFieldModelMap(savedFieldModel).values();
    }
}
