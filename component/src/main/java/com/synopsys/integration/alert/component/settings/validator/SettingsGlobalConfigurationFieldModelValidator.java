/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class SettingsGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);

        configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
            SettingsDescriptor.KEY_ENCRYPTION_PWD,
            SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT
        ));

        minimumEncryptionFieldLength(configurationFieldValidator, SettingsDescriptor.KEY_ENCRYPTION_PWD);
        minimumEncryptionFieldLength(configurationFieldValidator, SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);

        validateProxySettings(configurationFieldValidator);

        return configurationFieldValidator.getValidationResults();
    }

    private Set<AlertFieldStatus> validateProxySettings(ConfigurationFieldValidator configurationFieldValidator) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (configurationFieldValidator.fieldHasReadableValue(ProxyManager.KEY_PROXY_HOST)) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(ProxyManager.KEY_PROXY_PORT);
        }

        if (configurationFieldValidator.fieldHasReadableValue(ProxyManager.KEY_PROXY_PORT)) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(ProxyManager.KEY_PROXY_HOST);
            configurationFieldValidator.validateIsANumber(ProxyManager.KEY_PROXY_PORT);
        }

        if (configurationFieldValidator.fieldHasReadableValue(ProxyManager.KEY_PROXY_USERNAME)) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                ProxyManager.KEY_PROXY_PWD,
                ProxyManager.KEY_PROXY_HOST
            ));
        }

        if (configurationFieldValidator.fieldHasReadableValue(ProxyManager.KEY_PROXY_PWD)) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                ProxyManager.KEY_PROXY_USERNAME,
                ProxyManager.KEY_PROXY_HOST
            ));
        }

        if (configurationFieldValidator.fieldHasReadableValue(ProxyManager.KEY_PROXY_NON_PROXY_HOSTS)) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(ProxyManager.KEY_PROXY_HOST);
        }

        return statuses;
    }

    // TODO can add a common minimum length check to ConfigurationFieldValidator
    private void minimumEncryptionFieldLength(ConfigurationFieldValidator configurationFieldValidator, String fieldKey) {
        boolean hasValues = configurationFieldValidator.fieldHasReadableValue(fieldKey);
        String fieldValue = configurationFieldValidator.getStringValue(fieldKey).orElse("");
        if (hasValues && fieldValue.length() < 8) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(fieldKey, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT));
        }
    }

}
