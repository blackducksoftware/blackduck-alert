/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.security.authentication;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.UserRole;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

@Component
public class AlertAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(AlertAuthenticationProvider.class);
    private final DaoAuthenticationProvider alertDatabaseAuthProvider;
    private final LdapManager ldapManager;

    @Autowired
    public AlertAuthenticationProvider(final DaoAuthenticationProvider alertDatabaseAuthProvider, final LdapManager ldapManager) {
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
        this.ldapManager = ldapManager;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only UsernamePasswordAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }
        Authentication authenticationResult = performLdapAuthentication(authentication);
        if (!authenticationResult.isAuthenticated()) {
            authenticationResult = performDatabaseAuthentication(authentication);
        }
        final Collection<? extends GrantedAuthority> authorities = isAuthorized(authenticationResult) ? authenticationResult.getAuthorities() : List.of();
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authenticationResult.getPrincipal(), authenticationResult.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return authenticationToken;
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Authentication performDatabaseAuthentication(final Authentication pendingAuthentication) {
        logger.info("Attempting database authentication...");
        return alertDatabaseAuthProvider.authenticate(pendingAuthentication);
    }

    private Authentication performLdapAuthentication(final Authentication pendingAuthentication) {
        logger.info("Checking ldap based authentication...");
        Authentication result = pendingAuthentication;
        if (ldapManager.isLdapEnabled()) {
            logger.info("LDAP authentication enabled");
            try {
                final LdapAuthenticationProvider authenticationProvider = ldapManager.getAuthenticationProvider();
                result = authenticationProvider.authenticate(pendingAuthentication);
            } catch (final AlertLDAPConfigurationException ex) {
                logger.error("LDAP Configuration error", ex);
            } catch (final Exception ex) {
                logger.error("LDAP Authentication error", ex);
            }
        } else {
            logger.info("LDAP authentication disabled");
        }
        return result;
    }

    private boolean isAuthorized(final Authentication authentication) {
        final EnumSet<UserRole> allowedRoles = EnumSet.allOf(UserRole.class);
        return authentication.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                   .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX))
                   .map(UserRole::valueOf)
                   .anyMatch(allowedRoles::contains);
    }
}
