/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.startup.component;

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

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.environment.EnvironmentVariableProcessor;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
@Order(10)
public class AlertStartupInitializer extends StartupComponent {
    private static final String LINE_DIVIDER = "---------------------------------";
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final EnvironmentVariableUtility environmentUtility;
    private final DescriptorMap descriptorMap;
    private final DescriptorAccessor descriptorAccessor;
    private final ConfigurationModelConfigurationAccessor fieldConfigurationModelConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final FieldModelProcessor fieldModelProcessor;
    private final SettingsUtility settingsUtility;
    private final EnvironmentVariableProcessor environmentVariableProcessor;

    @Autowired
    public AlertStartupInitializer(DescriptorMap descriptorMap, EnvironmentVariableUtility environmentUtility, DescriptorAccessor descriptorAccessor, ConfigurationModelConfigurationAccessor fieldConfigurationModelConfigurationAccessor,
        ConfigurationFieldModelConverter modelConverter, FieldModelProcessor fieldModelProcessor, SettingsUtility settingsUtility, EnvironmentVariableProcessor environmentVariableProcessor) {
        this.descriptorMap = descriptorMap;
        this.environmentUtility = environmentUtility;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationModelConfigurationAccessor = fieldConfigurationModelConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.fieldModelProcessor = fieldModelProcessor;
        this.settingsUtility = settingsUtility;
        this.environmentVariableProcessor = environmentVariableProcessor;
    }

    @Override
    protected void initialize() {
        try {
            initializeConfigs();
            environmentVariableProcessor.updateConfigurations();
        } catch (Exception e) {
            logger.error("Error inserting startup values", e);
        }
    }

    @Deprecated(forRemoval = true, since = "6.9.0")
    private void initializeConfigs() throws IllegalArgumentException, SecurityException {
        logger.info(String.format("** %s **", LINE_DIVIDER));
        logger.info("Initializing descriptors with environment variables...");
        DescriptorKey settingsKey = settingsUtility.getKey();
        initializeConfiguration(List.of(settingsKey));
        List<DescriptorKey> descriptorKeys = descriptorMap.getDescriptorMap().keySet().stream().filter(key -> !key.equals(settingsKey)).collect(Collectors.toList());
        initializeConfiguration(descriptorKeys);
    }

    private void initializeConfiguration(Collection<DescriptorKey> descriptorKeys) {
        for (DescriptorKey descriptorKey : descriptorKeys) {
            logger.info(LINE_DIVIDER);
            logger.info("Descriptor: {}", descriptorKey.getUniversalKey());
            logger.info(LINE_DIVIDER);
            logger.info("  Starting Descriptor Initialization...");
            try {
                List<DefinedFieldModel> fieldsForDescriptor = descriptorAccessor.getFieldsForDescriptor(descriptorKey, ConfigContextEnum.GLOBAL).stream()
                                                                  .sorted(Comparator.comparing(DefinedFieldModel::getKey))
                                                                  .collect(Collectors.toList());
                List<ConfigurationModel> foundConfigurationModels = fieldConfigurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL);

                Map<String, ConfigurationFieldModel> existingConfiguredFields = new HashMap<>();
                foundConfigurationModels.forEach(config -> existingConfiguredFields.putAll(config.getCopyOfKeyToFieldMap()));

                Set<ConfigurationFieldModel> configurationModels = createFieldModelsFromDefinedFields(descriptorKey, fieldsForDescriptor, existingConfiguredFields);
                logConfiguration(configurationModels);
                updateConfigurationFields(descriptorKey, foundConfigurationModels, configurationModels);
            } catch (IllegalArgumentException | SecurityException | AlertException ex) {
                logger.error("error initializing descriptor", ex);
            } finally {
                logger.info("  Finished Descriptor Initialization...");
                logger.info(LINE_DIVIDER);
            }
        }
    }

    private Set<ConfigurationFieldModel> createFieldModelsFromDefinedFields(DescriptorKey descriptorKey, List<DefinedFieldModel> fieldsForDescriptor, Map<String, ConfigurationFieldModel> existingConfiguredFields) {
        Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        logger.info("  ### Environment Variables ### ");
        for (DefinedFieldModel fieldModel : fieldsForDescriptor) {
            String key = fieldModel.getKey();
            String convertedKey = environmentUtility.convertKeyToProperty(descriptorKey, key);
            boolean hasEnvironmentValue = environmentUtility.hasEnvironmentValue(convertedKey);
            logger.info("    {}", convertedKey);
            logger.debug("         Environment Variable Found - {}", hasEnvironmentValue);
            String defaultValue = null;
            if (existingConfiguredFields.containsKey(key)) {
                Optional<String> fieldValue = existingConfiguredFields.get(key).getFieldValue();
                if (fieldValue.isPresent()) {
                    defaultValue = fieldValue.get();
                }
            }

            environmentUtility.getEnvironmentValue(convertedKey, defaultValue)
                .flatMap(value -> modelConverter.convertFromDefinedFieldModel(fieldModel, value, StringUtils.isNotBlank(value)))
                .ifPresent(configurationModels::add);
        }
        return configurationModels;
    }

    private void updateConfigurationFields(DescriptorKey descriptorKey, List<ConfigurationModel> foundConfigurationModels, Set<ConfigurationFieldModel> configurationModels)
        throws AlertException {
        Optional<ConfigurationModel> optionalFoundModel = foundConfigurationModels
                                                              .stream()
                                                              .findFirst();
        logger.info("  ### Processing Configuration ###");
        if (!optionalFoundModel.isPresent()) {
            logger.info("    Writing initial values from environment.");
            Collection<ConfigurationFieldModel> savedFields = saveAction(descriptorKey, configurationModels);
            fieldConfigurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.GLOBAL, savedFields);
        }
    }

    private Collection<ConfigurationFieldModel> saveAction(DescriptorKey descriptorKey, Collection<ConfigurationFieldModel> configurationFieldModels) throws AlertException {
        Map<String, FieldValueModel> fieldValueModelMap = modelConverter.convertToFieldValuesMap(configurationFieldModels);
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        FieldModel savedFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
        return modelConverter.convertToConfigurationFieldModelMap(savedFieldModel).values();
    }

    private void logConfiguration(Collection<ConfigurationFieldModel> fieldModels) {
        if (!fieldModels.isEmpty()) {
            logger.info("  ");
            logger.info("  ### Configuration ### ");
            fieldModels.forEach(this::logField);
            logger.info("  ");
        }
    }

    private void logField(ConfigurationFieldModel fieldModel) {
        String value = fieldModel.isSensitive() ? "**********" : String.valueOf(fieldModel.getFieldValues());
        logger.info("    {} = {}", fieldModel.getFieldKey(), value);
    }

}
