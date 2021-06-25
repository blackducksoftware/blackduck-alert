/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class SettingsGlobalConfigurationValidator implements GlobalConfigurationValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        ConfigurationFieldValidator configurationFieldValidator = new ConfigurationFieldValidator(fieldModel);

        configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
            SettingsDescriptor.KEY_ENCRYPTION_PWD,
            SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT
        ));

        minimumEncryptionFieldLength(fieldModel, SettingsDescriptor.KEY_ENCRYPTION_PWD).ifPresent(statuses::add);
        minimumEncryptionFieldLength(fieldModel, SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).ifPresent(statuses::add);

        Set<AlertFieldStatus> proxyStatuses = validateProxySettings(configurationFieldValidator);
        statuses.addAll(proxyStatuses);

        return statuses;
    }

    private Set<AlertFieldStatus> validateProxySettings(ConfigurationFieldValidator configurationFieldValidator) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        FieldModel fieldModel = configurationFieldValidator.getFieldModel();

        if (hasValue(fieldModel, ProxyManager.KEY_PROXY_HOST)) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(ProxyManager.KEY_PROXY_PORT).ifPresent(statuses::add);
        }

        if (hasValue(fieldModel, ProxyManager.KEY_PROXY_PORT)) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(ProxyManager.KEY_PROXY_HOST).ifPresent(statuses::add);
            configurationFieldValidator.validateIsANumber(ProxyManager.KEY_PROXY_PORT).ifPresent(statuses::add);
        }

        if (hasValue(fieldModel, ProxyManager.KEY_PROXY_USERNAME)) {
            List<AlertFieldStatus> usernameStatuses = configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                ProxyManager.KEY_PROXY_PWD,
                ProxyManager.KEY_PROXY_HOST
            ));
            statuses.addAll(usernameStatuses);
        }

        if (hasValue(fieldModel, ProxyManager.KEY_PROXY_PWD)) {
            List<AlertFieldStatus> pwdStatuses = configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                ProxyManager.KEY_PROXY_USERNAME,
                ProxyManager.KEY_PROXY_HOST
            ));
            statuses.addAll(pwdStatuses);
        }

        return statuses;
    }

    private boolean hasValue(FieldModel fieldModel, String fieldKey) {
        return fieldModel.getFieldValueModel(fieldKey).map(FieldValueModel::hasValues).orElse(false);
    }

    private Optional<AlertFieldStatus> minimumEncryptionFieldLength(FieldModel fieldModel, String fieldKey) {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(fieldKey);
        Boolean hasValues = fieldValueModel.map(FieldValueModel::hasValues).orElse(false);
        String fieldValue = fieldValueModel.flatMap(FieldValueModel::getValue).orElse("");
        if (hasValues && fieldValue.length() < 8) {
            return Optional.of(AlertFieldStatus.error(fieldKey, SettingsDescriptor.FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT));
        }
        return Optional.empty();
    }
}
