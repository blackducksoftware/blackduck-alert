/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.component.authentication.security.event.AuthenticationEventManager;

public abstract class AuthenticationPerformer {
    private AuthenticationEventManager authenticationEventManager;
    private RoleAccessor roleAccessor;

    protected AuthenticationPerformer(AuthenticationEventManager authenticationEventManager, RoleAccessor roleAccessor) {
        this.authenticationEventManager = authenticationEventManager;
        this.roleAccessor = roleAccessor;
    }

    public final Optional<Authentication> performAuthentication(Authentication authentication) {
        Authentication authenticationResult = authenticateWithProvider(authentication);
        if (authenticationResult.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = isAuthorized(authenticationResult) ? authenticationResult.getAuthorities() : List.of();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authenticationResult.getPrincipal(), authenticationResult.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            authenticationEventManager.sendAuthenticationEvent(authenticationToken, getAuthenticationType());
            return Optional.of(authenticationToken);
        }
        return Optional.empty();
    }

    public abstract Authentication authenticateWithProvider(Authentication pendingAuthentication);

    public abstract AuthenticationType getAuthenticationType();

    private boolean isAuthorized(Authentication authentication) {
        Set<String> allowedRoles = roleAccessor.getRoles()
                                       .stream()
                                       .map(UserRoleModel::getName)
                                       .collect(Collectors.toSet());
        return authentication.getAuthorities()
                   .stream()
                   .map(authenticationEventManager::getRoleFromAuthority)
                   .flatMap(Optional::stream)
                   .anyMatch(allowedRoles::contains);
    }

}
