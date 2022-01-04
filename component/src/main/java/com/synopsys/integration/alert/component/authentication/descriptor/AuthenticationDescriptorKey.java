/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.descriptor;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class AuthenticationDescriptorKey extends DescriptorKey {
    public static final String AUTHENTICATION_COMPONENT = "component_authentication";

    public AuthenticationDescriptorKey() {
        super(AUTHENTICATION_COMPONENT, AuthenticationDescriptor.AUTHENTICATION_LABEL);
    }

}
