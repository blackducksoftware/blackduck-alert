/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.util.Stringable;

public class UserPrincipal extends Stringable implements UserDetails {
    private final UserModel userModel;

    public UserPrincipal(UserModel userModel) {
        this.userModel = userModel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userModel.getRoleAuthorities();
    }

    @Override
    public String getPassword() {
        return userModel.getPassword();
    }

    @Override
    public String getUsername() {
        return userModel.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !userModel.isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userModel.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !userModel.isPasswordExpired();
    }

    @Override
    public boolean isEnabled() {
        return userModel.isEnabled();
    }
}
