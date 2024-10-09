package com.blackduck.integration.alert.component.settings.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptor;

/**
 * @deprecated Global configuration validators will replace old FieldModel validators as Alert switches to a new concrete REST API. This class will be removed in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
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
