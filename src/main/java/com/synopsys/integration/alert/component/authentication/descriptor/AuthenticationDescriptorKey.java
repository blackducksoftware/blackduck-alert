package com.synopsys.integration.alert.component.authentication.descriptor;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;

@Component
public class AuthenticationDescriptorKey extends DescriptorKey {
    public static final String AUTHENTICATION_COMPONENT = "component_authentication";

    @Override
    public String getUniversalKey() {
        return AUTHENTICATION_COMPONENT;
    }
}
