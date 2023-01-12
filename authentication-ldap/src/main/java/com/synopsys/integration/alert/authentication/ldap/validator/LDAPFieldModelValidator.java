/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.authentication.ldap.validator;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.authentication.ldap.descriptor.LDAPDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class LDAPFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    @Autowired
    public LDAPFieldModelValidator() {
    }

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        boolean ldapEnabled = configurationFieldValidator.getBooleanValue(LDAPDescriptor.KEY_LDAP_ENABLED).orElse(false);

        if (ldapEnabled) {
            configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
                LDAPDescriptor.KEY_LDAP_SERVER,
                LDAPDescriptor.KEY_LDAP_MANAGER_DN,
                LDAPDescriptor.KEY_LDAP_MANAGER_PWD
            ));
        }

        return configurationFieldValidator.getValidationResults();
    }

}
