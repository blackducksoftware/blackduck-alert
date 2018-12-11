package com.synopsys.integration.alert.web.security.authentication.ldap;

import java.util.Set;

public class LdapConfiguration {
    private final boolean enabled;
    private final String server;
    private final String managerDN;
    private final String managerPassword;

    private final String authenticationType;
    private final String ldapReferral;

    private final String userSearchBase;
    private final String userSearchFilter;
    private final Set<String> userDNPatterns;
    private final Set<String> userAttributes;

    private final String groupSearchBase;
    private final String groupSearchFilter;
    private final String groupRoleAttribute;
    private final String rolePrefix;

    public LdapConfiguration(final boolean enabled, final String server, final String managerDN, final String managerPassword, final String authenticationType, final String ldapReferral,
        final String userSearchBase, final String userSearchFilter, final Set<String> userDNPatterns, final Set<String> userAttributes, final String groupSearchBase, final String groupSearchFilter, final String groupRoleAttribute,
        final String rolePrefix) {
        this.enabled = enabled;
        this.server = server;
        this.managerDN = managerDN;
        this.managerPassword = managerPassword;
        this.authenticationType = authenticationType;
        this.ldapReferral = ldapReferral;
        this.userSearchBase = userSearchBase;
        this.userSearchFilter = userSearchFilter;
        this.userDNPatterns = userDNPatterns;
        this.userAttributes = userAttributes;
        this.groupSearchBase = groupSearchBase;
        this.groupSearchFilter = groupSearchFilter;
        this.groupRoleAttribute = groupRoleAttribute;
        this.rolePrefix = rolePrefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getServer() {
        return server;
    }

    public String getManagerDN() {
        return managerDN;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getLdapReferral() {
        return ldapReferral;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public Set<String> getUserDNPatterns() {
        return userDNPatterns;
    }

    public Set<String> getUserAttributes() {
        return userAttributes;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }
}
