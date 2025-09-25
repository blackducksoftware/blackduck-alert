/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.validator;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraCloudGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    public static final Integer DEFAULT_JIRA_CLOUD_TIMEOUT_SECONDS = 300;

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_URL);
        configurationFieldValidator.validateIsAURL(JiraCloudDescriptor.KEY_JIRA_URL);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        configurationFieldValidator.validateIsANumber(JiraCloudDescriptor.KEY_JIRA_TIMEOUT);

        validateTimeout(configurationFieldValidator, fieldModel);

        return configurationFieldValidator.getValidationResults();
    }

    private void validateTimeout(ConfigurationFieldValidator configurationFieldValidator, FieldModel fieldModel) {
        // validate the timeout is a positive integer.
        Optional<String> timeoutValue = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_TIMEOUT);
        if (timeoutValue.isPresent()) {
            Integer timeoutSeconds = timeoutValue.map(NumberUtils::toInt)
                .orElse(DEFAULT_JIRA_CLOUD_TIMEOUT_SECONDS);
            if(timeoutSeconds < 1) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.error(JiraCloudDescriptor.KEY_JIRA_TIMEOUT, "Jira Cloud timeout must be a positive integer."));
            }
        } else {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(JiraCloudDescriptor.KEY_JIRA_TIMEOUT, ConfigurationFieldValidator.NOT_AN_INTEGER_VALUE));
        }
    }
}
