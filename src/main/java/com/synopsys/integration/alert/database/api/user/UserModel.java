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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserModel {
    private final String name;
    private final String password;
    private final List<GrantedAuthority> roles;

    public static final UserModel of(final String userName, final String password, final Collection<String> roles) {
        // Spring requires the roles to start with ROLE_
        final List<GrantedAuthority> roleAuthorities = roles.stream().map(role -> "ROLE_" + role).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UserModel(userName, password, roleAuthorities);
    }

    public static final UserModel of(final String userName, final String password, final List<GrantedAuthority> roles) {
        return new UserModel(userName, password, roles);
    }

    private UserModel(final String name, final String password, final List<GrantedAuthority> roles) {
        this.name = name;
        this.password = password;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public List<GrantedAuthority> getRoles() {
        return roles;
    }

    public boolean hasRole(final String role) {
        return roles.stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
