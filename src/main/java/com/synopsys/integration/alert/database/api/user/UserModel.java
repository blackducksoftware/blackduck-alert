/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.api.user;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.util.Stringable;

public class UserModel extends Stringable {
    public static final String ROLE_PREFIX = "ROLE_";
    private final String name;
    private final String password;
    private final Set<String> roles;
    private final boolean expired;
    private final boolean locked;
    private final boolean passwordExpired;
    private final boolean enabled;

    public static final UserModel of(final UserEntity userEntity, final Set<String> roles) {
        return new UserModel(userEntity.getUserName(), userEntity.getPassword(), roles, userEntity.isExpired(), userEntity.isLocked(), userEntity.isPasswordExpired(), userEntity.isEnabled());
    }

    public static final UserModel of(final String userName, final String password, final Set<String> roles) {
        return new UserModel(userName, password, roles, false, false, false, true);
    }

    public UserModel(final String name, final String password, final Set<String> roles, final boolean expired, final boolean locked, final boolean passwordExpired, final boolean enabled) {
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public Collection<GrantedAuthority> getRoleAuthorities() {
        // Spring requires the roles to start with ROLE_
        return roles.stream().map(role -> ROLE_PREFIX + role).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public boolean hasRole(final String role) {
        return roles.contains(role);
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
