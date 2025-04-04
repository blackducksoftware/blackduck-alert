/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.security.database;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.blackduck.integration.alert.common.persistence.accessor.UserAccessor;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.security.UserPrincipal;

@Service
public class UserDatabaseService implements UserDetailsService {
    private final UserAccessor userAccessor;

    @Autowired
    public UserDatabaseService(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return createUserDetails(username, userAccessor.getUser(username));
    }

    private UserDetails createUserDetails(String userName, Optional<UserModel> userModel) throws UsernameNotFoundException {
        UserModel model = userModel.orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", userName)));
        return new UserPrincipal(model);
    }
}
