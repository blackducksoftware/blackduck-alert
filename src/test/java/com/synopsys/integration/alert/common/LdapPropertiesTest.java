package com.synopsys.integration.alert.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LdapPropertiesTest {

    @Test
    public void testConstructor() {
        final String enabled = "true";
        final String server = "aserver";
        final String managerDN = "managerDN";
        final String managerPassword = "managerPassword";

        final String authenticationType = "simple";
        final String ldapReferral = "referral";

        final String userSearchBase = "searchbase";
        final String userSearchFilter = "searchFilter";
        final String userDNPatterns = "pattern1,pattern2";
        final String userAttributes = "attribute1,attribute2";

        final String groupSearchBase = "groupSearchBase";
        final String groupSearchFilter = "groupSearchFilter";
        final String groupRoleAttribute = "roleAttribute";
        final String rolePrefix = "ROLE_";
        final LdapProperties ldapConfig = new LdapProperties();

        ldapConfig.setEnabled(enabled);
        ldapConfig.setServer(server);
        ldapConfig.setManagerDN(managerDN);
        ldapConfig.setManagerPassword(managerPassword);
        ldapConfig.setAuthenticationType(authenticationType);
        ldapConfig.setLdapReferral(ldapReferral);
        ldapConfig.setUserSearchBase(userSearchBase);
        ldapConfig.setUserSearchFilter(userSearchFilter);
        ldapConfig.setUserDNPatterns(userDNPatterns);
        ldapConfig.setUserAttributes(userAttributes);
        ldapConfig.setGroupSearchBase(groupSearchBase);
        ldapConfig.setGroupSearchFilter(groupSearchFilter);
        ldapConfig.setGroupRoleAttribute(groupRoleAttribute);
        ldapConfig.setRolePrefix(rolePrefix);

        assertTrue(ldapConfig.isEnabled());
        assertEquals(server, ldapConfig.getServer());
        assertEquals(managerDN, ldapConfig.getManagerDN());
        assertEquals(managerPassword, ldapConfig.getManagerPassword());
        assertEquals(authenticationType, ldapConfig.getAuthenticationType());
        assertEquals(ldapReferral, ldapConfig.getLdapReferral());
        assertEquals(userSearchBase, ldapConfig.getUserSearchBase());
        assertEquals(userSearchFilter, ldapConfig.getUserSearchFilter());
        assertEquals(userDNPatterns, ldapConfig.getUserDNPatterns());
        assertEquals(userAttributes, ldapConfig.getUserAttributes());
        assertEquals(2, ldapConfig.getUserDNPatternArray().length);
        assertEquals(2, ldapConfig.getUserAttributeArray().length);
        assertEquals(groupSearchBase, ldapConfig.getGroupSearchBase());
        assertEquals(groupSearchFilter, ldapConfig.getGroupSearchFilter());
        assertEquals(groupRoleAttribute, ldapConfig.getGroupRoleAttribute());
        assertEquals(rolePrefix, ldapConfig.getRolePrefix());
    }
}
