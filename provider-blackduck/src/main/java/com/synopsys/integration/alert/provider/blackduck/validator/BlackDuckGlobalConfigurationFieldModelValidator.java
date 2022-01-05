/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@Component
public class BlackDuckGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public BlackDuckGlobalConfigurationFieldModelValidator(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
            ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME,
            BlackDuckDescriptor.KEY_BLACKDUCK_URL,
            BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY,
            BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT
        ));

        configurationFieldValidator.validateIsAURL(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        validateAPIToken(configurationFieldValidator);
        validateDuplicateNames(fieldModel).ifPresent(configurationFieldValidator::addValidationResults);
        validateTimeout(configurationFieldValidator);

        return configurationFieldValidator.getValidationResults();
    }

    private void validateAPIToken(ConfigurationFieldValidator configurationFieldValidator) {
        String apiKey = configurationFieldValidator.getStringValue(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY).orElse("");
        if (StringUtils.isNotBlank(apiKey) && (apiKey.length() < 64 || apiKey.length() > 256)) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, "Invalid Black Duck API Token."));
        }
    }

    private void validateTimeout(ConfigurationFieldValidator configurationFieldValidator) {
        boolean isANumberOrEmpty = configurationFieldValidator.getStringValue(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT)
            .map(NumberUtils::isCreatable)
            .orElse(true);
        if (isANumberOrEmpty) {
            int timeoutInt = configurationFieldValidator.getStringValue(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT)
                .map(NumberUtils::toInt)
                .orElse(BlackDuckProperties.DEFAULT_TIMEOUT);
            if (timeoutInt < 1) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.error(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, "Invalid timeout: The timeout must be a positive integer"));
            } else if (timeoutInt > 300) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.warning(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, "The provided timeout is greater than five minutes. Please ensure this is the desired behavior."));
            }
        } else {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, ConfigurationFieldValidator.NOT_AN_INTEGER_VALUE));
        }
    }

    private Optional<AlertFieldStatus> validateDuplicateNames(FieldModel fieldModel) {
        List<ConfigurationModel> configurations = configurationModelConfigurationAccessor.getConfigurationsByDescriptorType(DescriptorType.PROVIDER);
        if (configurations.isEmpty()) {
            return Optional.empty();
        }

        String configName = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse("");
        List<ConfigurationModel> modelsWithName = configurations.stream()
            .filter(configurationModel -> ConfigContextEnum.GLOBAL == configurationModel.getDescriptorContext())
            .filter(configurationModel -> configurationModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .filter(existingName -> existingName.equals(configName))
                .isPresent())
            .collect(Collectors.toList());
        Optional<AlertFieldStatus> duplicateError = Optional.of(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, "A provider configuration with this name already exists."));
        if (modelsWithName.size() > 1) {
            return duplicateError;
        } else if (modelsWithName.size() == 1) {
            boolean sameConfig = fieldModel.getId() != null && modelsWithName.stream()
                .findFirst()
                .map(ConfigurationModel::getConfigurationId)
                .map(id -> id.equals(Long.valueOf(fieldModel.getId())))
                .orElse(false);
            if (!sameConfig) {
                return duplicateError;
            }
        }
        return Optional.empty();
    }

}
