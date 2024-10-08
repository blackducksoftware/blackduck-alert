/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

/**
 * @deprecated Global configuration validators will replace old FieldModel validators as Alert switches to a new concrete REST API. This class will be removed in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraServerDescriptor.KEY_SERVER_URL);
        configurationFieldValidator.validateIsAURL(JiraServerDescriptor.KEY_SERVER_URL);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraServerDescriptor.KEY_SERVER_USERNAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(JiraServerDescriptor.KEY_SERVER_PASSWORD);

        return configurationFieldValidator.getValidationResults();
    }
}
