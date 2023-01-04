package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.AuthenticationSAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSAMLValidationAction {
    private final AuthenticationSAMLConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public AuthenticationSAMLValidationAction(AuthenticationSAMLConfigurationValidator validator, AuthorizationManager authorizationManager, AuthenticationDescriptorKey authenticationDescriptorKey) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
    }

    public ActionResponse<ValidationResponseModel> validate(AuthenticationSAMLConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource));
    }
}
