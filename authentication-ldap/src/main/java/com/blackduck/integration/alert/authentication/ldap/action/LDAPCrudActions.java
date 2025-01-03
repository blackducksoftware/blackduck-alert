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
import com.blackduck.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class LDAPCrudActions {
    private final ConfigurationCrudHelper configurationCrudHelper;
    private final LDAPConfigAccessor ldapConfigAccessor;
    private final LDAPConfigurationValidator ldapConfigurationValidator;

    @Autowired
    public LDAPCrudActions(
        AuthorizationManager authorizationManager,
        LDAPConfigAccessor ldapConfigAccessor,
        LDAPConfigurationValidator ldapConfigurationValidator,
        AuthenticationDescriptorKey authenticationDescriptorKey
    ) {
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.ldapConfigAccessor = ldapConfigAccessor;
        this.ldapConfigurationValidator = ldapConfigurationValidator;
    }

    public ActionResponse<LDAPConfigModel> getOne() {
        return configurationCrudHelper.getOne(
            ldapConfigAccessor::getConfiguration);
    }

    public ActionResponse<LDAPConfigModel> create(LDAPConfigModel ldapConfigModel) {
        return configurationCrudHelper.create(
            () -> ldapConfigurationValidator.validate(ldapConfigModel),
            ldapConfigAccessor::doesConfigurationExist,
            () -> ldapConfigAccessor.createConfiguration(ldapConfigModel)
        );
    }

    public ActionResponse<LDAPConfigModel> update(LDAPConfigModel ldapConfigModel) {
        return configurationCrudHelper.update(
            () -> ldapConfigurationValidator.validate(ldapConfigModel),
            ldapConfigAccessor::doesConfigurationExist,
            () -> ldapConfigAccessor.updateConfiguration(ldapConfigModel)
        );
    }

    public ActionResponse<LDAPConfigModel> delete() {
        return configurationCrudHelper.delete(
            ldapConfigAccessor::doesConfigurationExist,
            ldapConfigAccessor::deleteConfiguration
        );
    }
}
