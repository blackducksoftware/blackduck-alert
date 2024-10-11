/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

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
