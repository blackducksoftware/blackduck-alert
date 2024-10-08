/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.users;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public class UserManagementDescriptorKey extends DescriptorKey {
    public static final String USER_MANAGEMENT_COMPONENT = "component_users";

    public UserManagementDescriptorKey() {
        super(USER_MANAGEMENT_COMPONENT, UserManagementDescriptor.USER_MANAGEMENT_LABEL);
    }

}
