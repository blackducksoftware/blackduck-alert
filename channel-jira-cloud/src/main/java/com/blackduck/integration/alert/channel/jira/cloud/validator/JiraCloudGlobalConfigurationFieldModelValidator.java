/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraCloudGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_URL);
        configurationFieldValidator.validateIsAURL(JiraCloudDescriptor.KEY_JIRA_URL);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);

        return configurationFieldValidator.getValidationResults();
    }

}
