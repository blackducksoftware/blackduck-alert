package com.synopsys.integration.alert.component.authentication.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

@Component
public class AuthenticationConfigurationAction extends ConfigurationAction {

    @Autowired
    protected AuthenticationConfigurationAction(final AuthenticationDescriptorKey descriptorKey, AuthenticationApiAction authenticationApiAction) {
        super(descriptorKey);
        addGlobalApiAction(authenticationApiAction);
    }
}
