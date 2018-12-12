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
package com.synopsys.integration.alert.web.security.authentication.ldap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.support.DigestMd5DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.LdapProperties;

@Component
public class LdapManager {
    private LdapContextSource contextSource;
    private LdapAuthenticationProvider authenticationProvider;
    private LdapProperties currentConfiguration;

    @Autowired
    public LdapManager(final LdapProperties ldapProperties) {
        updateConfiguration(ldapProperties);
    }

    public boolean isLdapEnabled() {
        return currentConfiguration == null ? false : currentConfiguration.isEnabled();
    }

    public LdapProperties getCurrentConfiguration() {
        return currentConfiguration;
    }

    public LdapAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void updateConfiguration(final LdapProperties configuration) {
        this.currentConfiguration = configuration;
        if (currentConfiguration.isEnabled()) {
            updateContext();
        }
    }

    public void updateContext() {
        final LdapProperties configuration = getCurrentConfiguration();
        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(configuration.getServer());
        ldapContextSource.setUserDn(configuration.getManagerDN());
        ldapContextSource.setPassword(configuration.getManagerPassword());
        ldapContextSource.setReferral(configuration.getLdapReferral());
        ldapContextSource.setAuthenticationStrategy(createAuthenticationStrategy(configuration));
        contextSource = ldapContextSource;
        contextSource.afterPropertiesSet();
        updateAuthenticationProvider();
    }

    private DirContextAuthenticationStrategy createAuthenticationStrategy(final LdapProperties configuration) {
        final String authenticationType = configuration.getAuthenticationType();
        DirContextAuthenticationStrategy strategy = null;
        if (StringUtils.isNotBlank(authenticationType)) {
            if ("digest".equals(authenticationType)) {
                strategy = new DigestMd5DirContextAuthenticationStrategy();
            } else if ("simple".equals(authenticationType)) {
                strategy = new SimpleDirContextAuthenticationStrategy();
            }
        }

        return strategy;
    }

    private void updateAuthenticationProvider() {
        final LdapAuthenticator authenticator = createAuthenticator();
        final LdapAuthoritiesPopulator authoritiesPopulator = createAuthoritiesPopulator();
        authenticationProvider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    private LdapAuthenticator createAuthenticator() {
        final BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(createLdapUserSearch(contextSource));
        authenticator.setUserDnPatterns(currentConfiguration.getUserDNPatternArray());
        authenticator.setUserAttributes(currentConfiguration.getUserAttributeArray());
        return authenticator;
    }

    private LdapAuthoritiesPopulator createAuthoritiesPopulator() {
        final DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource, currentConfiguration.getGroupSearchBase());
        authoritiesPopulator.setGroupSearchFilter(currentConfiguration.getGroupSearchFilter());
        authoritiesPopulator.setGroupRoleAttribute(currentConfiguration.getGroupRoleAttribute());
        authoritiesPopulator.setRolePrefix(currentConfiguration.getRolePrefix());
        return authoritiesPopulator;
    }

    private LdapUserSearch createLdapUserSearch(final LdapContextSource contextSource) {
        final LdapProperties configuration = getCurrentConfiguration();
        LdapUserSearch userSearch = null;
        if (StringUtils.isNotBlank(configuration.getUserSearchFilter())) {
            userSearch = new FilterBasedLdapUserSearch(configuration.getUserSearchBase(), configuration.getUserSearchFilter(), contextSource);
        }
        return userSearch;
    }
}
