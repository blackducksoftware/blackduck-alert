/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.ldap;

import java.util.Optional;

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
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.component.authentication.security.UserManagementAuthoritiesPopulator;

@Component
public class LdapManager {
    private final AuthenticationDescriptorKey authenticationDescriptorKey;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final UserManagementAuthoritiesPopulator authoritiesPopulator;
    private final InetOrgPersonContextMapper inetOrgPersonContextMapper;

    @Autowired
    public LdapManager(
        AuthenticationDescriptorKey authenticationDescriptorKey,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        UserManagementAuthoritiesPopulator authoritiesPopulator,
        InetOrgPersonContextMapper inetOrgPersonContextMapper
    ) {
        this.authenticationDescriptorKey = authenticationDescriptorKey;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.authoritiesPopulator = authoritiesPopulator;
        this.inetOrgPersonContextMapper = inetOrgPersonContextMapper;
    }

    public boolean isLdapEnabled() {
        Optional<ConfigurationModel> ldapConfig = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(authenticationDescriptorKey)
                                                      .stream()
                                                      .findFirst();
        return ldapConfig.map(configurationModel -> Boolean.valueOf(getFieldValueOrEmpty(configurationModel, AuthenticationDescriptor.KEY_LDAP_ENABLED)))
                   .orElse(false);
    }

    public FieldUtility getCurrentConfiguration() throws AlertConfigurationException {
        ConfigurationModel configModel = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(authenticationDescriptorKey)
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(() -> new AlertConfigurationException("Settings configuration missing"));
        return new FieldUtility(configModel.getCopyOfKeyToFieldMap());
    }

    public Optional<LdapAuthenticationProvider> getAuthenticationProvider() throws AlertConfigurationException {
        FieldUtility fieldUtility = getCurrentConfiguration();
        return createAuthProvider(fieldUtility);
    }

    public Optional<LdapAuthenticationProvider> createAuthProvider(FieldUtility configuration) throws AlertConfigurationException {
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
            LdapAuthenticationProvider ldapAuthenticationProvider = updateAuthenticationProvider(configuration, ldapContextSource);
            return Optional.of(ldapAuthenticationProvider);
        } catch (IllegalArgumentException ex) {
            throw new AlertConfigurationException("Error creating LDAP Context Source", ex);
        }
    }

    private String getFieldValueOrEmpty(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
    }

    private DirContextAuthenticationStrategy createAuthenticationStrategy(FieldUtility configuration) {
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

    private LdapAuthenticationProvider updateAuthenticationProvider(FieldUtility configurationModel, LdapContextSource contextSource) throws AlertConfigurationException {
        LdapAuthenticator authenticator = createAuthenticator(configurationModel, contextSource);
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = createAuthoritiesPopulator(configurationModel, contextSource);

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator, ldapAuthoritiesPopulator);
        ldapAuthenticationProvider.setUserDetailsContextMapper(inetOrgPersonContextMapper);
        return ldapAuthenticationProvider;
    }

    private LdapAuthenticator createAuthenticator(FieldUtility configurationModel, LdapContextSource contextSource) throws AlertConfigurationException {
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

    private LdapAuthoritiesPopulator createAuthoritiesPopulator(FieldUtility configurationModel, LdapContextSource contextSource) {
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

    private LdapUserSearch createLdapUserSearch(FieldUtility configurationModel, LdapContextSource contextSource) {
        LdapUserSearch userSearch = null;
        String userSearchFilter = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER);
        String userSearchBase = configurationModel.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE);
        if (StringUtils.isNotBlank(userSearchFilter)) {
            userSearch = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, contextSource);
        }
        return userSearch;
    }

}
