/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

@Component
public class UserManagementDescriptor extends ComponentDescriptor {
    public static final String USER_MANAGEMENT_LABEL = "User Management";
    public static final String USER_MANAGEMENT_URL = "users";
    public static final String USER_MANAGEMENT_DESCRIPTION = "This page allows you to configure users and roles for Alert.";
    public static final String USERS_COMPONENT_NAMESPACE = "users.UserManagement";

    @Autowired
    public UserManagementDescriptor(UserManagementDescriptorKey descriptorKey) {
        super(descriptorKey);
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.empty();
    }

}
