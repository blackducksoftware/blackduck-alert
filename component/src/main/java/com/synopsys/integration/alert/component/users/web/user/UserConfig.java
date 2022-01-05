/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.user;

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
    private boolean external;
    private String authenticationType;

    public UserConfig() {
    }

    public UserConfig(String id, String username, String password, String emailAddress, Set<String> roleNames, boolean expired, boolean locked, boolean passwordExpired, boolean enabled, boolean passwordSet,
        String authenticationType, boolean external) {
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
        this.authenticationType = authenticationType;
        this.external = external;
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

    public boolean isExternal() {
        return external;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }
}
