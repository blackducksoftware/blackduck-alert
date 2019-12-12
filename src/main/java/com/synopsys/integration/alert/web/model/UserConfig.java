/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.model;

import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.Config;

public class UserConfig extends Config {
    private String username;
    private String password;
    private String emailAddress;
    private Set<String> roleNames;
    private boolean expired;
    private boolean locked;
    private boolean passwordExpired;
    private boolean enabled;
    private boolean passwordSet;

    public UserConfig() {
    }

    public UserConfig(String id, String username, String password, String emailAddress, Set<String> roleNames, boolean expired, boolean locked, boolean passwordExpired, boolean enabled, boolean passwordSet) {
        super(id);
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.roleNames = roleNames;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
        this.passwordSet = passwordSet;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Set<String> getRoleNames() {
        return roleNames;
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

    public boolean isPasswordSet() {
        return passwordSet;
    }
}
