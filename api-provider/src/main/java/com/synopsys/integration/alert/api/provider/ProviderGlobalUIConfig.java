/*
 * api-provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class ProviderGlobalUIConfig extends UIConfig {
    private static final String ERROR_DUPLICATE_PROVIDER_NAME = "A provider configuration with this name already exists.";

    private final ProviderKey providerKey;
    private final ConfigurationAccessor configurationAccessor;

    public ProviderGlobalUIConfig(ProviderKey providerKey, String label, String description, String urlName, ConfigurationAccessor configurationAccessor) {
        super(label, description, urlName);
        this.providerKey = providerKey;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public final List<ConfigField> createFields() {
        ConfigField providerConfigEnabled = new CheckboxConfigField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED, ProviderDescriptor.LABEL_PROVIDER_CONFIG_ENABLED, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_ENABLED)
                                                .applyDefaultValue(Boolean.TRUE.toString());
        ConfigField providerConfigName = new TextInputConfigField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_NAME)
                                             .applyRequired(true)
                                             .applyValidationFunctions(this::validateDuplicateNames);

        List<ConfigField> providerCommonGlobalFields = List.of(providerConfigEnabled, providerConfigName);
        List<ConfigField> providerGlobalFields = createProviderGlobalFields();
        return Stream.concat(providerCommonGlobalFields.stream(), providerGlobalFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderGlobalFields();

    public ProviderKey getProviderKey() {
        return providerKey;
    }

    private ValidationResult validateDuplicateNames(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        List<String> errorList = List.of();
        List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorType(DescriptorType.PROVIDER);
        if (configurations.isEmpty()) {
            return ValidationResult.success();
        }

        List<ConfigurationModel> modelsWithName = configurations.stream()
                                                      .filter(configurationModel -> ConfigContextEnum.GLOBAL == configurationModel.getDescriptorContext())
                                                      .filter(configurationModel ->
                                                                  configurationModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                                                                      .flatMap(ConfigurationFieldModel::getFieldValue)
                                                                      .filter(configName -> configName.equals(fieldToValidate.getValue().orElse("")))
                                                                      .isPresent())
                                                      .collect(Collectors.toList());
        if (modelsWithName.size() > 1) {
            errorList = List.of(ERROR_DUPLICATE_PROVIDER_NAME);
        } else if (modelsWithName.size() == 1) {
            boolean sameConfig = fieldModel.getId() != null && modelsWithName.get(0).getConfigurationId().equals(Long.valueOf(fieldModel.getId()));
            if (!sameConfig) {
                errorList = List.of(ERROR_DUPLICATE_PROVIDER_NAME);
            }
        }
        return ValidationResult.errors(errorList);
    }

}
