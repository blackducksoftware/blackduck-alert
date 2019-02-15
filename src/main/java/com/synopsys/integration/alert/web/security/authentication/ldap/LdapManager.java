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
package com.synopsys.integration.alert.web.security.authentication.ldap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertLDAPConfigurationException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;

@Component
public class LdapManager {
    private static final Logger logger = LoggerFactory.getLogger(LdapManager.class);
    private final BaseConfigurationAccessor configurationAccessor;
    private LdapContextSource contextSource;
    private LdapAuthenticationProvider authenticationProvider;

    @Autowired
    public LdapManager(final BaseConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    public boolean isLdapEnabled() {
        boolean enabled = false;
        try {
            enabled = Boolean.valueOf(getFieldValueOrEmpty(getCurrentConfiguration(), SettingsDescriptor.KEY_LDAP_ENABLED));
        } catch (final AlertDatabaseConstraintException | AlertLDAPConfigurationException ex) {
            logger.warn(ex.getMessage());
            logger.debug("cause: ", ex);
        }

        return enabled;
    }

    public ConfigurationModel getCurrentConfiguration() throws AlertDatabaseConstraintException, AlertLDAPConfigurationException {
        return configurationAccessor.getConfigurationsByDescriptorName(SettingsDescriptor.SETTINGS_COMPONENT)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertLDAPConfigurationException("Settings configuration missing"));
    }

    public LdapAuthenticationProvider getAuthenticationProvider() throws AlertLDAPConfigurationException {
        updateContext();
        return authenticationProvider;
    }

    public void updateContext() throws AlertLDAPConfigurationException {
        try {
            if (!isLdapEnabled()) {
                return;
            } else {
                final ConfigurationModel configuration = getCurrentConfiguration();
                final LdapContextSource ldapContextSource = new LdapContextSource();

                final String ldapServer = getFieldValueOrEmpty(configuration, SettingsDescriptor.KEY_LDAP_SERVER);
                final String managerDN = getFieldValueOrEmpty(configuration, SettingsDescriptor.KEY_LDAP_MANAGER_DN);
                final String managerPassword = getFieldValueOrEmpty(configuration, SettingsDescriptor.KEY_LDAP_MANAGER_PWD);
                final String ldapReferral = getFieldValueOrEmpty(configuration, SettingsDescriptor.KEY_LDAP_REFERRAL);
                if (StringUtils.isNotBlank(ldapServer)) {
                    ldapContextSource.setUrl(ldapServer);
                    ldapContextSource.setUserDn(managerDN);
                    ldapContextSource.setPassword(managerPassword);
                    ldapContextSource.setReferral(ldapReferral);
                    ldapContextSource.setAuthenticationStrategy(createAuthenticationStrategy(configuration));
                }
                contextSource = ldapContextSource;
                contextSource.afterPropertiesSet();
                updateAuthenticationProvider(configuration);
            }
        } catch (final IllegalArgumentException | AlertDatabaseConstraintException ex) {
            throw new AlertLDAPConfigurationException("Error creating LDAP Context Source", ex);
        }
    }

    private String getFieldValueOrEmpty(final ConfigurationModel configurationModel, final String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(field -> field.getFieldValue()).orElse("");
    }

    private DirContextAuthenticationStrategy createAuthenticationStrategy(final ConfigurationModel configuration) {
        final String authenticationType = getFieldValueOrEmpty(configuration, SettingsDescriptor.KEY_LDAP_AUTHENTICATION_TYPE);
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

    private void updateAuthenticationProvider(final ConfigurationModel configurationModel) throws AlertLDAPConfigurationException {
        final LdapAuthenticator authenticator = createAuthenticator(configurationModel);
        final LdapAuthoritiesPopulator authoritiesPopulator = createAuthoritiesPopulator(configurationModel);
        authenticationProvider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    private LdapAuthenticator createAuthenticator(final ConfigurationModel configurationModel) throws AlertLDAPConfigurationException {
        final BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        try {
            final String[] userDnArray = createArrayFromCSV(getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_USER_DN_PATTERNS));
            final String[] userAttributeArray = createArrayFromCSV(getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_USER_ATTRIBUTES));
            authenticator.setUserSearch(createLdapUserSearch(configurationModel, contextSource));
            authenticator.setUserDnPatterns(userDnArray);
            authenticator.setUserAttributes(userAttributeArray);
            authenticator.afterPropertiesSet();
        } catch (final Exception ex) {
            throw new AlertLDAPConfigurationException("Error creating LDAP authenticator", ex);
        }
        return authenticator;
    }

    private String[] createArrayFromCSV(final String commaSeparatedString) {
        return StringUtils.split(commaSeparatedString, ",");
    }

    private LdapAuthoritiesPopulator createAuthoritiesPopulator(final ConfigurationModel configurationModel) {
        final String groupSearchBase = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_BASE);
        final String groupSearchFilter = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER);
        final String groupRoleAttribute = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE);
        final String rolePrefix = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_ROLE_PREFIX);
        final DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        authoritiesPopulator.setGroupSearchFilter(groupSearchFilter);
        authoritiesPopulator.setGroupRoleAttribute(groupRoleAttribute);
        authoritiesPopulator.setRolePrefix(rolePrefix);
        return authoritiesPopulator;
    }

    private LdapUserSearch createLdapUserSearch(final ConfigurationModel configurationModel, final LdapContextSource contextSource) {
        LdapUserSearch userSearch = null;
        final String userSearchFilter = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        final String userSearchBase = getFieldValueOrEmpty(configurationModel, SettingsDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        if (StringUtils.isNotBlank(userSearchFilter)) {
            userSearch = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, contextSource);
        }
        return userSearch;
    }
}
