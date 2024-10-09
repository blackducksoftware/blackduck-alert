/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.validator;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;

/**
 * @deprecated Global configuration validators will replace old FieldModel validators as Alert switches to a new concrete REST API. This class will be removed in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class EmailGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        configurationFieldValidator.validateRequiredFieldIsNotBlank(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());

        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey());
        configurationFieldValidator.validateIsANumber(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey());

        boolean useAuth = fieldModel.getFieldValueModel(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey())
            .flatMap(FieldValueModel::getValue)
            .map(Boolean::valueOf)
            .orElse(false);

        if (useAuth) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(),
                EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()
            ));
        }

        return configurationFieldValidator.getValidationResults();
    }
}
