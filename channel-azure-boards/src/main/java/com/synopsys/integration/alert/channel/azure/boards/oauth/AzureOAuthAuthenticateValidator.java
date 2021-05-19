/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class AzureOAuthAuthenticateValidator implements ConfigValidationFunction {
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public AzureOAuthAuthenticateValidator(OAuthRequestValidator oAuthRequestValidator) {
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        if (oAuthRequestValidator.hasRequests()) {
            return ValidationResult.errors("Authentication in Progress cannot perform current action.");
        }
        return ValidationResult.success();
    }
}
