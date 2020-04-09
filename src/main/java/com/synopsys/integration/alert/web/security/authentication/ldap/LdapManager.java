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
package com.synopsys.integration.alert.web.security.authentication.ldap;

import java.util.Optional;

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
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;

@Component
public class LdapManager {
    private static final Logger logger = LoggerFactory.getLogger(LdapManager.class);

    private final AuthenticationDescriptorKey authenticationDescriptorKey;
    private final ConfigurationAccessor configurationAccessor;
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;

    @Autowired
    public LdapManager(AuthenticationDescriptorKey AuthenticationDescriptorKey, ConfigurationAccessor configurationAccessor, UserManagementAuthoritiesPopulator authoritiesPopulator) {
        this.authenticationDescriptorKey = AuthenticationDescriptorKey;
        this.configurationAccessor = configurationAccessor;
        this.authoritiesPopulator = authoritiesPopulator;
    }

    public boolean isLdapEnabled() {
        try {
            Optional<ConfigurationModel> ldapConfig = configurationAccessor.getConfigurationsByDescriptorKey(authenticationDescriptorKey)
                                                          .stream()
                                                          .findFirst();
            if (ldapConfig.isPresent()) {
                return Boolean.valueOf(getFieldValueOrEmpty(ldapConfig.get(), AuthenticationDescriptor.KEY_LDAP_ENABLED));
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.warn(ex.getMessage());
            logger.debug("cause: ", ex);
        }

        return false;
    }

    public FieldAccessor getCurrentConfiguration() throws AlertDatabaseConstraintException, AlertConfigurationException {
        ConfigurationModel configModel = configurationAccessor.getConfigurationsByDescriptorKey(authenticationDescriptorKey)
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(() -> new AlertConfigurationException("Settings configuration missing"));
        return new FieldAccessor(configModel.getCopyOfKeyToFieldMap());
    }

    public Optional<LdapAuthenticationProvider> getAuthenticationProvider() throws AlertConfigurationException {
        try {
            FieldAccessor fieldAccessor = getCurrentConfiguration();
            return createAuthProvider(fieldAccessor);
        } catch (AlertDatabaseConstraintException ex) {
            throw new AlertConfigurationException("Error creating LDAP Context Source", ex);
        }
    }

    public Optional<LdapAuthenticationProvider> createAuthProvider(FieldAccessor configuration) throws AlertConfigurationException {
        try {
            boolean enabled = configuration.getBooleanOrFalse(AuthenticationDescriptor.KEY_LDAP_ENABLED);
            if (!enabled) {
                return Optional.empty();
            }
            LdapContextSource ldapContextSource = new LdapContextSource();

            String ldapServer = configuration.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_SERVER);
            String managerDN = configuration.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN);
            String managerPassword = configuration.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD);
            String ldapReferral = configuration.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_REFERRAL);
            if (StringUtils.isNotBlank(ldapServer)) {
                ldapContextSource.setUrl(ldapServer);
                ldapContextSource.setUserDn(managerDN);
                ldapContextSource.setPassword(managerPassword);
                ldapContextSource.setReferral(ldapReferral);
                ldapContextSource.setAuthenticationStrategy(createAuthenticationStrategy(configuration));
            }
            ldapContextSource.afterPropertiesSet();
            return Optional.of(updateAuthenticationProvider(configuration, ldapContextSource));
        } catch (IllegalArgumentException ex) {
            throw new AlertConfigurationException("Error creating LDAP Context Source", ex);
        }
    }

    private String getFieldValueOrEmpty(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
    }

    private DirContextAuthenticationStrategy createAuthenticationStrategy(FieldAccessor configuration) {
        String authenticationType = configuration.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE);
        DirContextAuthenticationStrategy strategy = null;
        if (StringUtils.isNotBlank(authenticationType)) {
            if ("digest".equalsIgnoreCase(authenticationType)) {
                strategy = new DigestMd5DirContextAuthenticationStrategy();
            } else if ("simple".equalsIgnoreCase(authenticationType)) {
                strategy = new SimpleDirContextAuthenticationStrategy();
            }
        }

        return strategy;
    }

    private LdapAuthenticationProvider updateAuthenticationProvider(FieldAccessor configurationModel, LdapContextSource contextSource) throws AlertConfigurationException {
        LdapAuthenticator authenticator = createAuthenticator(configurationModel, contextSource);
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = createAuthoritiesPopulator(configurationModel, contextSource);
        return new LdapAuthenticationProvider(authenticator, ldapAuthoritiesPopulator);
    }

    private LdapAuthenticator createAuthenticator(FieldAccessor configurationModel, LdapContextSource contextSource) throws AlertConfigurationException {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        try {
            String[] userDnArray = createArrayFromCSV(configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS));
            String[] userAttributeArray = createArrayFromCSV(configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_ATTRIBUTES));
            authenticator.setUserSearch(createLdapUserSearch(configurationModel, contextSource));
            authenticator.setUserDnPatterns(userDnArray);
            authenticator.setUserAttributes(userAttributeArray);
            authenticator.afterPropertiesSet();
        } catch (Exception ex) {
            throw new AlertConfigurationException("Error creating LDAP authenticator", ex);
        }
        return authenticator;
    }

    private String[] createArrayFromCSV(String commaSeparatedString) {
        return StringUtils.split(commaSeparatedString, ",");
    }

    private LdapAuthoritiesPopulator createAuthoritiesPopulator(FieldAccessor configurationModel, LdapContextSource contextSource) {
        String groupSearchBase = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_BASE);
        String groupSearchFilter = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER);
        String groupRoleAttribute = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE);
        MappingLdapAuthoritiesPopulator mappingLdapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, groupSearchBase, this.authoritiesPopulator);
        mappingLdapAuthoritiesPopulator.setGroupSearchFilter(groupSearchFilter);
        mappingLdapAuthoritiesPopulator.setGroupRoleAttribute(groupRoleAttribute);
        // expect the LDAP group name for the role to be ROLE_<ROLE_NAME> where ROLE_NAME defined in UserRoles
        // Set the prefix to the empty string because the prefix is by default set to ROLE_ we don't want the populator to create ROLE_ROLE_<ROLE_NAME> due to the default prefix
        mappingLdapAuthoritiesPopulator.setRolePrefix("");
        return mappingLdapAuthoritiesPopulator;
    }

    private LdapUserSearch createLdapUserSearch(FieldAccessor configurationModel, LdapContextSource contextSource) {
        LdapUserSearch userSearch = null;
        String userSearchFilter = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        String userSearchBase = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        if (StringUtils.isNotBlank(userSearchFilter)) {
            userSearch = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, contextSource);
        }
        return userSearch;
    }

}
