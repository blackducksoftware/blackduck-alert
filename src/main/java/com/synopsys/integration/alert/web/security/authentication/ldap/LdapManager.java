package com.synopsys.integration.alert.web.security.authentication.ldap;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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

@Component
public class LdapManager {
    private LdapContextSource contextSource;
    private LdapAuthenticationProvider authenticationProvider;
    private LdapConfiguration currentConfiguration;

    public boolean isLdapEnabled() {
        return currentConfiguration == null ? false : currentConfiguration.isEnabled();
    }

    public LdapConfiguration getCurrentConfiguration() {
        return currentConfiguration;
    }

    public LdapAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void updateConfiguration(final LdapConfiguration configuration) {
        this.currentConfiguration = configuration;
        updateContext();
    }

    public void updateContext() {
        final LdapConfiguration configuration = getCurrentConfiguration();
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

    private DirContextAuthenticationStrategy createAuthenticationStrategy(final LdapConfiguration configuration) {
        final String authenticationType = configuration.getAuthenticationType();
        DirContextAuthenticationStrategy strategy = null;
        if (StringUtils.isNotBlank(authenticationType)) {
            if (authenticationType.equals("digest")) {
                strategy = new DigestMd5DirContextAuthenticationStrategy();
            } else if (authenticationType.equals("simple")) {
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
        authenticator.setUserDnPatterns(createUserDnPatterns());
        authenticator.setUserAttributes(createUserAttributes());
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
        final LdapConfiguration configuration = getCurrentConfiguration();
        LdapUserSearch userSearch = null;
        if (StringUtils.isNotBlank(configuration.getUserSearchFilter())) {
            userSearch = new FilterBasedLdapUserSearch(configuration.getUserSearchBase(), configuration.getUserSearchFilter(), contextSource);
        }
        return userSearch;
    }

    private String[] createUserDnPatterns() {
        return createArray(currentConfiguration.getUserDNPatterns());
    }

    private String[] createUserAttributes() {
        return createArray(currentConfiguration.getUserAttributes());
    }

    private String[] createArray(final Set<String> setToConvert) {
        String[] patterns = null;
        if (setToConvert != null) {
            patterns = new String[setToConvert.size()];
            patterns = setToConvert.toArray(patterns);
        }
        return patterns;
    }
}
