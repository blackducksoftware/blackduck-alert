/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.CustomUIConfig;

@Component
public class UserManagementUIConfig extends CustomUIConfig {

    public UserManagementUIConfig() {
        super(UserManagementDescriptor.USER_MANAGEMENT_LABEL, UserManagementDescriptor.USER_MANAGEMENT_DESCRIPTION, UserManagementDescriptor.USER_MANAGEMENT_URL, UserManagementDescriptor.USERS_COMPONENT_NAMESPACE);
    }
}
