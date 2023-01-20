package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SAMLValidationAction {
    private final SAMLConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public SAMLValidationAction(SAMLConfigurationValidator validator, AuthorizationManager authorizationManager, AuthenticationDescriptorKey authenticationDescriptorKey) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
    }

    public ActionResponse<ValidationResponseModel> validate(SAMLConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource));
    }
}
