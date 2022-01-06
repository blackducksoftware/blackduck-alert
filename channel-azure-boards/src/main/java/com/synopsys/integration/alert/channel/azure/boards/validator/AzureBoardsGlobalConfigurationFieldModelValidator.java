/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
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
