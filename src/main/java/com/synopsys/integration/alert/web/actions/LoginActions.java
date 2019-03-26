/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.actions;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.database.api.user.UserRole;
import com.synopsys.integration.alert.web.model.LoginConfig;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

@Component
public class LoginActions {
    private static final Logger logger = LoggerFactory.getLogger(LoginActions.class);
    private final DaoAuthenticationProvider alertDatabaseAuthProvider;
    private final LdapManager ldapManager;

    @Autowired
    public LoginActions(final DaoAuthenticationProvider alertDatabaseAuthProvider, final LdapManager ldapManager) {
        this.alertDatabaseAuthProvider = alertDatabaseAuthProvider;
        this.ldapManager = ldapManager;
    }

    public boolean authenticateUser(final LoginConfig loginConfig) throws BadCredentialsException {
        Authentication authentication;
        authentication = performLdapAuthentication(loginConfig);
        if (!authentication.isAuthenticated()) {
            authentication = performDatabaseAuthentication(loginConfig);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication.isAuthenticated() && isAuthorized(authentication);
    }

    private Authentication performDatabaseAuthentication(final LoginConfig loginConfig) {
        logger.info("Attempting database authentication...");
        final Authentication pendingAuthentication = createUsernamePasswordAuthToken(loginConfig);
        return alertDatabaseAuthProvider.authenticate(pendingAuthentication);
    }

    private Authentication performLdapAuthentication(final LoginConfig loginConfig) {
        logger.info("Checking ldap based authentication...");
        final Authentication pendingAuthentication = createUsernamePasswordAuthToken(loginConfig);
        Authentication result;
        if (ldapManager.isLdapEnabled()) {
            logger.info("LDAP authentication enabled");
            try {
                final LdapAuthenticationProvider authenticationProvider = ldapManager.getAuthenticationProvider();
                result = authenticationProvider.authenticate(pendingAuthentication);
            } catch (final AlertLDAPConfigurationException ex) {
                logger.error("LDAP Configuration error", ex);
                result = pendingAuthentication;
            } catch (final Exception ex) {
                logger.error("LDAP Authentication error", ex);
                result = pendingAuthentication;
            }
        } else {
            logger.info("LDAP authentication disabled");
            result = pendingAuthentication;
        }
        return result;
    }

    private boolean isAuthorized(final Authentication authentication) {
        EnumSet<UserRole> allowedRoles = EnumSet.allOf(UserRole.class);
        return authentication.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .filter(role -> role.startsWith(UserModel.ROLE_PREFIX))
                   .map(role -> StringUtils.substringAfter(role, UserModel.ROLE_PREFIX))
                   .anyMatch(roleName -> allowedRoles.contains(UserRole.valueOf(roleName)));
    }

    private UsernamePasswordAuthenticationToken createUsernamePasswordAuthToken(final LoginConfig loginConfig) {
        return new UsernamePasswordAuthenticationToken(loginConfig.getBlackDuckUsername(), loginConfig.getBlackDuckPassword());
    }
}
