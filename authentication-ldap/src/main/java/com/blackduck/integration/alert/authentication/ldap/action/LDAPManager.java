package com.blackduck.integration.alert.authentication.ldap.action;

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
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.authentication.ldap.MappingLDAPAuthoritiesPopulator;
import com.blackduck.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;

@Component
public class LDAPManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final LDAPConfigAccessor ldapConfigAccessor;
    private final UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator;
    private final InetOrgPersonContextMapper inetOrgPersonContextMapper;

    @Autowired
    public LDAPManager(
        LDAPConfigAccessor ldapConfigAccessor,
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator,
        InetOrgPersonContextMapper inetOrgPersonContextMapper
    ) {
        this.ldapConfigAccessor = ldapConfigAccessor;
        this.userManagementAuthoritiesPopulator = userManagementAuthoritiesPopulator;
        this.inetOrgPersonContextMapper = inetOrgPersonContextMapper;
    }

    public Boolean isLDAPEnabled() {
        if (getCurrentConfiguration().isEmpty()) {
            return false;
        }

        return getCurrentConfiguration().get().getEnabled();
    }

    public Optional<LDAPConfigModel> getCurrentConfiguration() {
        return ldapConfigAccessor.getConfiguration();
    }

    public Optional<LdapAuthenticationProvider> getAuthenticationProvider() throws AlertConfigurationException {
        Optional<LDAPConfigModel> currentConfiguration = getCurrentConfiguration();
        if (currentConfiguration.isPresent()) {
            return createAuthProvider(currentConfiguration.get());
        } else {
            return Optional.empty();
        }
    }

    public Optional<LdapAuthenticationProvider> createAuthProvider(LDAPConfigModel ldapConfigModel) throws AlertConfigurationException {
        try {
            LdapContextSource ldapContextSource = new LdapContextSource();

            Boolean ldapEnabled = ldapConfigModel.getEnabled();
            String ldapServer = ldapConfigModel.getServerName();
            String managerDN = ldapConfigModel.getManagerDn();
            String managerPassword = ldapConfigModel.getManagerPassword().orElse("");
            String ldapReferral = ldapConfigModel.getReferral().orElse("");

            if (Boolean.FALSE.equals(ldapEnabled) || StringUtils.isBlank(ldapServer) || StringUtils.isBlank(managerDN) || StringUtils.isBlank(managerPassword)) {
                return Optional.empty();
            }

            ldapContextSource.setUrl(ldapServer);
            ldapContextSource.setUserDn(managerDN);
            ldapContextSource.setPassword(managerPassword);
            ldapContextSource.setReferral(ldapReferral);
            ldapContextSource.setAuthenticationStrategy(createAuthenticationStrategy(ldapConfigModel));

            ldapContextSource.afterPropertiesSet();
            LdapAuthenticationProvider ldapAuthenticationProvider = updateAuthenticationProvider(ldapConfigModel, ldapContextSource);
            return Optional.of(ldapAuthenticationProvider);
        } catch (IllegalArgumentException ex) {
            throw new AlertConfigurationException("Error creating LDAP Context Source", ex);
        }
    }

    private DirContextAuthenticationStrategy createAuthenticationStrategy(LDAPConfigModel ldapConfigModel) {
        String authenticationType = ldapConfigModel.getAuthenticationType().orElse("");
        DirContextAuthenticationStrategy strategy;
        logger.debug("LDAP Authentication Strategy: {}", authenticationType);

        if ("none".equalsIgnoreCase(authenticationType)) {
            strategy = null;
        } else if ("digest".equalsIgnoreCase(authenticationType)) {
            strategy = new DigestMd5DirContextAuthenticationStrategy();
        } else {
            strategy = new SimpleDirContextAuthenticationStrategy();
        }

        return strategy;
    }

    private LdapAuthenticationProvider updateAuthenticationProvider(LDAPConfigModel ldapConfigModel, LdapContextSource ldapContextSource) throws AlertConfigurationException {
        LdapAuthenticator ldapAuthenticator = createAuthenticator(ldapConfigModel, ldapContextSource);
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = createAuthoritiesPopulator(ldapConfigModel, ldapContextSource);

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(ldapAuthenticator, ldapAuthoritiesPopulator);
        ldapAuthenticationProvider.setUserDetailsContextMapper(inetOrgPersonContextMapper);
        return ldapAuthenticationProvider;
    }

    private LdapAuthenticator createAuthenticator(LDAPConfigModel ldapConfigModel, LdapContextSource ldapContextSource) throws AlertConfigurationException {
        BindAuthenticator bindAuthenticator = new BindAuthenticator(ldapContextSource);
        try {
            String[] userDnArray = createArrayFromCSV(ldapConfigModel.getUserDnPatterns().orElse(""));
            String[] userAttributeArray = createArrayFromCSV(ldapConfigModel.getUserAttributes().orElse(""));
            bindAuthenticator.setUserSearch(createLdapUserSearch(ldapConfigModel, ldapContextSource));
            bindAuthenticator.setUserDnPatterns(userDnArray);
            bindAuthenticator.setUserAttributes(userAttributeArray);
            bindAuthenticator.afterPropertiesSet();
        } catch (Exception ex) {
            throw new AlertConfigurationException("Error creating LDAP authenticator", ex);
        }
        return bindAuthenticator;
    }

    private String[] createArrayFromCSV(String commaSeparatedString) {
        return StringUtils.split(commaSeparatedString, ",");
    }

    private LdapAuthoritiesPopulator createAuthoritiesPopulator(LDAPConfigModel ldapConfigModel, LdapContextSource ldapContextSource) {
        String groupSearchBase = ldapConfigModel.getGroupSearchBase().orElse("");
        String groupSearchFilter = ldapConfigModel.getGroupSearchFilter().orElse("");
        String groupRoleAttribute = ldapConfigModel.getGroupRoleAttribute().orElse("");
        MappingLDAPAuthoritiesPopulator mappingLDAPAuthoritiesPopulator = new MappingLDAPAuthoritiesPopulator(
            ldapContextSource,
            groupSearchBase,
            this.userManagementAuthoritiesPopulator
        );
        mappingLDAPAuthoritiesPopulator.setGroupSearchFilter(groupSearchFilter);
        mappingLDAPAuthoritiesPopulator.setGroupRoleAttribute(groupRoleAttribute);
        // expect the LDAP group name for the role to be ROLE_<ROLE_NAME> where ROLE_NAME defined in UserRoles
        // Set the prefix to the empty string because the prefix is by default set to ROLE_ we don't want the populator to create ROLE_ROLE_<ROLE_NAME> due to the default prefix
        mappingLDAPAuthoritiesPopulator.setRolePrefix("");
        return mappingLDAPAuthoritiesPopulator;
    }

    private LdapUserSearch createLdapUserSearch(LDAPConfigModel ldapConfigModel, LdapContextSource ldapContextSource) {
        LdapUserSearch ldapUserSearch = null;
        String userSearchFilter = ldapConfigModel.getUserSearchFilter().orElse("");
        String userSearchBase = ldapConfigModel.getUserSearchBase().orElse("");
        if (StringUtils.isNotBlank(userSearchFilter)) {
            ldapUserSearch = new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, ldapContextSource);
        }
        return ldapUserSearch;
    }

}
