/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;

public class UserModel extends AlertSerializableModel {
    public static final String ROLE_PREFIX = "ROLE_";
    private final Long id;
    private final String name;
    private final String password;
    private final String emailAddress;
    private final Set<UserRoleModel> roles;
    private final Set<String> roleNames;
    private final boolean expired;
    private final boolean locked;
    private final boolean passwordExpired;
    private final boolean enabled;
    private final AuthenticationType authenticationType;

    private UserModel(Long id, String name, String password, String emailAddress, Set<UserRoleModel> roles, boolean expired, boolean locked, boolean passwordExpired, boolean enabled, AuthenticationType authenticationType) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.emailAddress = emailAddress;
        this.roles = roles;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
        this.authenticationType = authenticationType;
        if (null == roles || roles.isEmpty()) {
            this.roleNames = Set.of();
        } else {
            this.roleNames = roles.stream().map(UserRoleModel::getName).collect(Collectors.toSet());
        }
    }

    public static UserModel newUser(String userName, String password, String emailAddress, AuthenticationType authenticationType, Set<UserRoleModel> roles, boolean enabled) {
        return existingUser(null, userName, password, emailAddress, authenticationType, roles, enabled);
    }

    public static UserModel existingUser(Long id, String userName, String password, String emailAddress, AuthenticationType authenticationType, Set<UserRoleModel> roles, boolean enabled) {
        return new UserModel(id, userName, password, emailAddress, roles, false, false, false, enabled, authenticationType);
    }

    public Long getId() {
        return id;
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

    public Set<UserRoleModel> getRoles() {
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

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public boolean isExternal() {
        return AuthenticationType.DATABASE != authenticationType;
    }

}
