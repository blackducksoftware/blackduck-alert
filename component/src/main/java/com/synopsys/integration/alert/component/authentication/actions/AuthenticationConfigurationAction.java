/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
public class AuthenticationConfigurationAction extends ConfigurationAction {

    @Autowired
    protected AuthenticationConfigurationAction(AuthenticationDescriptorKey descriptorKey, AuthenticationApiAction authenticationApiAction, AuthenticationFieldModelTestAction authenticationFieldModelTestAction) {
        super(descriptorKey);
        addGlobalApiAction(authenticationApiAction);
        addGlobalTestAction(authenticationFieldModelTestAction);

    }
}
