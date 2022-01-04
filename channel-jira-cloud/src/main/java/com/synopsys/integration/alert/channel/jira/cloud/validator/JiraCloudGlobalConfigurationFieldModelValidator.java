/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

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
