/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class UserModel extends AlertSerializableModel {
    public static final String ROLE_PREFIX = "ROLE_";
    private final String name;
    private final String password;
    private final String emailAddress;
    private final Set<UserRoleModel> roles;
    private final Set<String> roleNames;
    private final boolean expired;
    private final boolean locked;
    private final boolean passwordExpired;
    private final boolean enabled;

    private UserModel(String name, String password, String emailAddress, Set<UserRoleModel> roles, boolean expired, boolean locked, boolean passwordExpired, boolean enabled) {
        this.name = name;
        this.password = password;
        this.emailAddress = emailAddress;
        this.roles = roles;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
        if (null == roles || roles.isEmpty()) {
            this.roleNames = Set.of();
        } else {
            this.roleNames = roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet());
        }
    }

    public static final UserModel of(String userName, String password, String emailAddress, Set<UserRoleModel> roles, boolean enabled) {
        return new UserModel(userName, password, emailAddress, roles, false, false, false, enabled);
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Collection<UserRoleModel> getRoles() {
        return roles;
    }

    public Collection<GrantedAuthority> getRoleAuthorities() {
        // Spring requires the roles to start with ROLE_
        return roles.stream()
                   .map(UserRoleModel::getName)
                   .map(role -> ROLE_PREFIX + role)
                   .map(SimpleGrantedAuthority::new)
                   .collect(Collectors.toList());
    }

    public boolean hasRole(String role) {
        return roleNames.contains(role);
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
