/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.blackduck.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

/**
 * @deprecated Global configuration validators will replace old FieldModel validators as Alert switches to a new concrete REST API. This class will be removed in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsGlobalConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public AzureBoardsGlobalConfigurationFieldModelValidator(OAuthRequestValidator oAuthRequestValidator) {
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(AzureBoardsDescriptor.KEY_CLIENT_ID);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(AzureBoardsDescriptor.KEY_CLIENT_SECRET);

        if (oAuthRequestValidator.hasRequests()) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(AzureBoardsDescriptor.KEY_OAUTH, "Authentication in progress cannot perform current action."));
        }

        return configurationFieldValidator.getValidationResults();
    }

}
