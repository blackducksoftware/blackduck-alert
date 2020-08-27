package com.synopsys.integration.alert.channel.azure.boards.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class AzureOAuthAuthenticateValidator implements ConfigValidationFunction {
    private OAuthRequestValidator oAuthRequestValidator;

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
