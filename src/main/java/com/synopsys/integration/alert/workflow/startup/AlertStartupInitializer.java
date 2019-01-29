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
package com.synopsys.integration.alert.workflow.startup;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

@Component
public class AlertStartupInitializer {
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final Environment environment;
    private final DescriptorMap descriptorMap;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final BaseConfigurationAccessor fieldConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public AlertStartupInitializer(final DescriptorMap descriptorMap, final Environment environment, final BaseDescriptorAccessor descriptorAccessor, final BaseConfigurationAccessor fieldConfigurationAccessor,
        final ConfigurationFieldModelConverter modelConverter) {
        this.descriptorMap = descriptorMap;
        this.environment = environment;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationAccessor = fieldConfigurationAccessor;
        this.modelConverter = modelConverter;
    }

    public void initializeConfigs() throws IllegalArgumentException, SecurityException {
        logger.info("** --------------------------------- **");
        logger.info("Initializing descriptors with environment variables...");
        boolean overwriteCurrentConfig = isEnvironmentOverrideEnabled();
        logger.info("Environment variables override configuration: {}", overwriteCurrentConfig);
        initializeConfiguration(List.of(SettingsDescriptor.SETTINGS_COMPONENT), overwriteCurrentConfig);
        List<String> descriptorNames = descriptorMap.getDescriptorMap().keySet().stream().filter(key -> !key.equals(SettingsDescriptor.SETTINGS_COMPONENT)).sorted().collect(Collectors.toList());
        initializeConfiguration(descriptorNames, overwriteCurrentConfig);
    }

    private boolean isEnvironmentOverrideEnabled() {
        boolean environmentOverride = false;
        try {
            // determine if the environment variables should overwrite based on the settings configuration.
            Optional<ConfigurationModel> settingsConfiguration = findSettingsConfiguration();
            final String fieldKey = SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE;
            boolean overwriteSavedInDB = settingsConfiguration
                                             .flatMap(configurationModel -> configurationModel.getField(fieldKey))
                                             .flatMap(field -> field.getFieldValue())
                                             .map(value -> Boolean.valueOf(value)).orElse(Boolean.FALSE);
            final String environmentFieldKey = convertKeyToPropery(SettingsDescriptor.SETTINGS_COMPONENT, fieldKey);
            Optional<String> environmentValue = getEnvironmentValue(environmentFieldKey);

            environmentOverride = environmentValue.map(envValue -> Boolean.valueOf(envValue)).orElse(overwriteSavedInDB);
        } catch (AlertDatabaseConstraintException ex) {
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
            } catch (IllegalArgumentException | SecurityException | AlertDatabaseConstraintException ex) {
                logger.error("error initializing descriptor", ex);
            }
        }
    }

    private Set<ConfigurationFieldModel> createFieldModelsFromDefinedFields(final String descriptorName, List<DefinedFieldModel> fieldsForDescriptor) {
        final Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        for (final DefinedFieldModel fieldModel : fieldsForDescriptor) {
            final String key = fieldModel.getKey();
            final String convertedKey = convertKeyToPropery(descriptorName, key);
            boolean hasEnvironmentValue = hasEnvironmentValue(convertedKey);
            logger.info("  {}", convertedKey);
            logger.debug("       Environment Variable Found - {}", hasEnvironmentValue);
            getEnvironmentValue(convertedKey)
                .flatMap(value -> modelConverter.convertFromDefinedFieldModel(fieldModel, value))
                .ifPresent(configurationModels::add);
        }
        return configurationModels;
    }

    private void updateConfigurationFields(final String descriptorName, boolean overwriteCurrentConfig, final List<ConfigurationModel> foundConfigurationModels, final Set<ConfigurationFieldModel> configurationModels)
        throws AlertDatabaseConstraintException {
        if (!configurationModels.isEmpty()) {
            if (!foundConfigurationModels.isEmpty()) {
                if (overwriteCurrentConfig) {
                    final ConfigurationModel configurationModel = foundConfigurationModels.get(0);
                    logger.info("  Overwriting configuration values with environment for descriptor.");
                    fieldConfigurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), configurationModels);
                }
            } else {
                logger.info("  Writing initial configuration values from environment for descriptor.");
                fieldConfigurationAccessor.createConfiguration(descriptorName, ConfigContextEnum.GLOBAL, configurationModels);

            }
        }
    }

    private String convertKeyToPropery(final String descriptorName, final String key) {
        final String keyUnderscores = key.replace(".", "_");
        return String.join("_", "alert", descriptorName, keyUnderscores).toUpperCase();
    }

    private boolean hasEnvironmentValue(final String propertyKey) {
        String value = System.getProperty(propertyKey);
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
