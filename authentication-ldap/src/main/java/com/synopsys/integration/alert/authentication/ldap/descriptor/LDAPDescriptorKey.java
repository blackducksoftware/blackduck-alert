/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.authentication.ldap.descriptor;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class LDAPDescriptorKey extends DescriptorKey {
    public static final String AUTHENTICATION_COMPONENT = "component_authentication";

    public LDAPDescriptorKey() {
        super(AUTHENTICATION_COMPONENT, LDAPDescriptor.AUTHENTICATION_LABEL);
    }

}
