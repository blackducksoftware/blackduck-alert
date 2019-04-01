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
package com.synopsys.integration.alert.workflow.startup;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.web.config.FieldModelProcessor;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

@Component
public class AlertStartupInitializer {
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final Environment environment;
    private final DescriptorMap descriptorMap;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final BaseConfigurationAccessor fieldConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final FieldModelProcessor fieldModelProcessor;

    @Autowired
    public AlertStartupInitializer(final DescriptorMap descriptorMap, final Environment environment, final BaseDescriptorAccessor descriptorAccessor, final BaseConfigurationAccessor fieldConfigurationAccessor,
        final ConfigurationFieldModelConverter modelConverter, final FieldModelProcessor fieldModelProcessor) {
        this.descriptorMap = descriptorMap;
        this.environment = environment;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationAccessor = fieldConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.fieldModelProcessor = fieldModelProcessor;
    }

    public void initializeConfigs() throws IllegalArgumentException, SecurityException {
        logger.info("** --------------------------------- **");
        logger.info("Initializing descriptors with environment variables...");
        final boolean overwriteCurrentConfig = manageEnvironmentOverrideEnabled();
        logger.info("Environment variables override configuration: {}", overwriteCurrentConfig);
        initializeConfiguration(List.of(SettingsDescriptor.SETTINGS_COMPONENT), overwriteCurrentConfig);
        final List<String> descriptorNames = descriptorMap.getDescriptorMap().keySet().stream().filter(key -> !key.equals(SettingsDescriptor.SETTINGS_COMPONENT)).sorted().collect(Collectors.toList());
        initializeConfiguration(descriptorNames, overwriteCurrentConfig);
    }

    private boolean manageEnvironmentOverrideEnabled() {
        boolean environmentOverride = false;
        try {
            // determine if the environment variables should overwrite based on the settings configuration.
            Optional<ConfigurationModel> settingsConfiguration = findSettingsConfiguration();
            final String fieldKey = SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE;

            final String environmentFieldKey = convertKeyToProperty(SettingsDescriptor.SETTINGS_COMPONENT, fieldKey);
            final Optional<String> environmentValue = getEnvironmentValue(environmentFieldKey);

            if (environmentValue.isPresent() && settingsConfiguration.isPresent()) {
                ConfigurationModel foundModel = settingsConfiguration.get();
                Map<String, ConfigurationFieldModel> fieldModelMap = foundModel.getCopyOfKeyToFieldMap();
                fieldModelMap.get(fieldKey).setFieldValue(String.valueOf(Boolean.valueOf(environmentValue.get())));
                settingsConfiguration = Optional.ofNullable(fieldConfigurationAccessor.updateConfiguration(foundModel.getConfigurationId(), fieldModelMap.values()));
            }

            environmentOverride = settingsConfiguration
                                      .flatMap(configurationModel -> configurationModel.getField(fieldKey))
                                      .flatMap(ConfigurationFieldModel::getFieldValue)
                                      .map(value -> Boolean.valueOf(value)).orElse(Boolean.FALSE);
        } catch (final AlertDatabaseConstraintException ex) {
            logger.error("Error checking environment override", ex);
        }
        return environmentOverride;
    }

    private Optional<ConfigurationModel> findSettingsConfiguration() throws AlertDatabaseConstraintException {
        final List<ConfigurationModel> settingsConfigurationModels = fieldConfigurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL);
        return settingsConfigurationModels.stream().findFirst();
    }

    private void initializeConfiguration(final Collection<String> descriptorNames, final boolean overwriteCurrentConfig) {
        for (final String descriptorName : descriptorNames) {
            logger.info("---------------------------------");
            logger.info("Descriptor: {}", descriptorName);
            logger.info("---------------------------------");
            try {
                final List<DefinedFieldModel> fieldsForDescriptor = descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.GLOBAL).stream()
                                                                        .sorted(Comparator.comparing(DefinedFieldModel::getKey))
                                                                        .collect(Collectors.toList());
                final Set<ConfigurationFieldModel> configurationModels = createFieldModelsFromDefinedFields(descriptorName, fieldsForDescriptor);
                final List<ConfigurationModel> foundConfigurationModels = fieldConfigurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
                updateConfigurationFields(descriptorName, overwriteCurrentConfig, foundConfigurationModels, configurationModels);
            } catch (final IllegalArgumentException | SecurityException | AlertDatabaseConstraintException ex) {
                logger.error("error initializing descriptor", ex);
            }
        }
    }

    private Set<ConfigurationFieldModel> createFieldModelsFromDefinedFields(final String descriptorName, final List<DefinedFieldModel> fieldsForDescriptor) {
        final Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        for (final DefinedFieldModel fieldModel : fieldsForDescriptor) {
            final String key = fieldModel.getKey();
            final String convertedKey = convertKeyToProperty(descriptorName, key);
            final boolean hasEnvironmentValue = hasEnvironmentValue(convertedKey);
            logger.info("  {}", convertedKey);
            logger.debug("       Environment Variable Found - {}", hasEnvironmentValue);
            getEnvironmentValue(convertedKey)
                .flatMap(value -> modelConverter.convertFromDefinedFieldModel(fieldModel, value, StringUtils.isNotBlank(value)))
                .ifPresent(configurationModels::add);
        }
        return configurationModels;
    }

    private void updateConfigurationFields(final String descriptorName, final boolean overwriteCurrentConfig, final List<ConfigurationModel> foundConfigurationModels, final Set<ConfigurationFieldModel> configurationModels)
        throws AlertDatabaseConstraintException {
        if (!configurationModels.isEmpty()) {
            if (!foundConfigurationModels.isEmpty()) {
                final ConfigurationModel foundModel = foundConfigurationModels.get(0);
                logger.info("  Overwriting configuration values with environment for descriptor.");
                final Collection<ConfigurationFieldModel> updatedFields = updateAction(descriptorName, configurationModels, foundModel, overwriteCurrentConfig);
                fieldConfigurationAccessor.updateConfiguration(foundModel.getConfigurationId(), updatedFields);
            } else {
                logger.info("  Writing initial configuration values from environment for descriptor.");
                final Collection<ConfigurationFieldModel> savedFields = saveAction(descriptorName, configurationModels);
                fieldConfigurationAccessor.createConfiguration(descriptorName, ConfigContextEnum.GLOBAL, savedFields);
            }
        }
    }

    private Collection<ConfigurationFieldModel> updateAction(final String descriptorName, final Collection<ConfigurationFieldModel> configurationFieldModels, final ConfigurationModel foundModel, final boolean overwriteCurrentConfig)
        throws AlertDatabaseConstraintException {
        final Collection<ConfigurationFieldModel> fieldsToUpdate;
        if (overwriteCurrentConfig) {
            fieldsToUpdate = configurationFieldModels;
        } else {
            fieldsToUpdate = new LinkedList<>();
            for (ConfigurationFieldModel fieldModel : configurationFieldModels) {
                Optional<ConfigurationFieldModel> currentFieldModel = foundModel.getField(fieldModel.getFieldKey());
                currentFieldModel.ifPresentOrElse(fieldsToUpdate::add, () -> fieldsToUpdate.add(fieldModel));
            }
        }

        final Map<String, FieldValueModel> fieldValueModelMap = fieldModelProcessor.convertToFieldValuesMap(fieldsToUpdate);
        final FieldModel fieldModel = new FieldModel(descriptorName, ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        final FieldModel updatedFieldModel = fieldModelProcessor.performUpdateAction(fieldModel);
        return modelConverter.convertFromFieldModel(updatedFieldModel).values();
    }

    private Collection<ConfigurationFieldModel> saveAction(final String descriptorName, final Collection<ConfigurationFieldModel> configurationFieldModels) throws AlertDatabaseConstraintException {
        final Map<String, FieldValueModel> fieldValueModelMap = fieldModelProcessor.convertToFieldValuesMap(configurationFieldModels);
        final FieldModel fieldModel = new FieldModel(descriptorName, ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        final FieldModel savedFieldModel = fieldModelProcessor.performSaveAction(fieldModel);
        return modelConverter.convertFromFieldModel(savedFieldModel).values();
    }

    private String convertKeyToProperty(final String descriptorName, final String key) {
        final String keyUnderscores = key.replace(".", "_");
        return String.join("_", "alert", descriptorName, keyUnderscores).toUpperCase();
    }

    private boolean hasEnvironmentValue(final String propertyKey) {
        final String value = System.getProperty(propertyKey);
        return StringUtils.isNotBlank(value) || environment.containsProperty(propertyKey);
    }

    private Optional<String> getEnvironmentValue(final String propertyKey) {
        String value = System.getProperty(propertyKey);
        if (StringUtils.isBlank(value)) {
            value = environment.getProperty(propertyKey);
        }

        return Optional.ofNullable(value);
    }
}
