package com.synopsys.integration.alert.api.authentication.descriptor;


import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class AuthenticationDescriptorKey extends DescriptorKey {
    public static final String AUTHENTICATION_COMPONENT = "component_authentication";

    public AuthenticationDescriptorKey() {
        super(AUTHENTICATION_COMPONENT, AuthenticationDescriptor.AUTHENTICATION_LABEL);
    }

}
