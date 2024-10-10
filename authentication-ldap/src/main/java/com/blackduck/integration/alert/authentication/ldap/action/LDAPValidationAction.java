/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class LDAPValidationAction {
    private final LDAPConfigurationValidator ldapConfigurationValidator;
    private final ConfigurationValidationHelper configurationValidationHelper;

    @Autowired
    public LDAPValidationAction(
        LDAPConfigurationValidator ldapConfigurationValidator,
        AuthorizationManager authorizationManager,
        AuthenticationDescriptorKey authenticationDescriptorKey
    ) {
        this.ldapConfigurationValidator = ldapConfigurationValidator;
        this.configurationValidationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
    }

    public ActionResponse<ValidationResponseModel> validate(LDAPConfigModel requestResource) {
        return configurationValidationHelper.validate(() -> ldapConfigurationValidator.validate(requestResource));
    }
}
