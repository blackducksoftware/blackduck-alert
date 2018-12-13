package com.synopsys.integration.alert.web.security.authentication.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.Test;

import com.synopsys.integration.alert.common.LdapProperties;

public class LdapManagerTest {

    @Test
    public void testUpdate() {
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

        final LdapProperties properties = new LdapProperties();
        properties.setEnabled(enabled);
        properties.setServer(server);
        properties.setManagerDN(managerDN);
        properties.setManagerPassword(managerPassword);
        properties.setAuthenticationType(authenticationType);
        properties.setLdapReferral(ldapReferral);
        properties.setUserSearchBase(userSearchBase);
        properties.setUserSearchFilter(userSearchFilter);
        properties.setUserDNPatterns(userDNPatterns);
        properties.setUserAttributes(userAttributes);
        properties.setGroupSearchBase(groupSearchBase);
        properties.setGroupSearchFilter(groupSearchFilter);
        properties.setGroupRoleAttribute(groupRoleAttribute);
        properties.setRolePrefix(rolePrefix);
        final LdapManager ldapManager = new LdapManager(properties);
        final LdapProperties updatedProperties = ldapManager.getCurrentConfiguration();
        assertEquals(server, updatedProperties.getServer());
        assertEquals(managerDN, updatedProperties.getManagerDN());
        assertEquals(managerPassword, updatedProperties.getManagerPassword());
        assertEquals(authenticationType, updatedProperties.getAuthenticationType());
        assertEquals(ldapReferral, updatedProperties.getLdapReferral());
        assertEquals(userSearchBase, updatedProperties.getUserSearchBase());
        assertEquals(userSearchFilter, updatedProperties.getUserSearchFilter());
        assertEquals(userDNPatterns, updatedProperties.getUserDNPatterns());
        assertEquals(userAttributes, updatedProperties.getUserAttributes());
        assertEquals(2, updatedProperties.getUserDNPatternArray().length);
        assertEquals(2, updatedProperties.getUserAttributeArray().length);
        assertEquals(groupSearchBase, updatedProperties.getGroupSearchBase());
        assertEquals(groupSearchFilter, updatedProperties.getGroupSearchFilter());
        assertEquals(groupRoleAttribute, updatedProperties.getGroupRoleAttribute());
        assertEquals(rolePrefix, updatedProperties.getRolePrefix());
    }

    @Test
    public void testIsEnabled() {
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

        final LdapProperties properties = new LdapProperties();
        properties.setEnabled(true);
        properties.setServer(server);
        properties.setManagerDN(managerDN);
        properties.setManagerPassword(managerPassword);
        properties.setAuthenticationType(authenticationType);
        properties.setLdapReferral(ldapReferral);
        properties.setUserSearchBase(userSearchBase);
        properties.setUserSearchFilter(userSearchFilter);
        properties.setUserDNPatterns(userDNPatterns);
        properties.setUserAttributes(userAttributes);
        properties.setGroupSearchBase(groupSearchBase);
        properties.setGroupSearchFilter(groupSearchFilter);
        properties.setGroupRoleAttribute(groupRoleAttribute);
        properties.setRolePrefix(rolePrefix);
        final LdapManager ldapManager = new LdapManager(properties);
        assertTrue(ldapManager.isLdapEnabled());
        properties.setEnabled(false);
        ldapManager.updateConfiguration(properties);
        assertFalse(ldapManager.isLdapEnabled());
    }

    @Test
    public void testAuthenticationTypeDigest() {
        final String server = "aserver";
        final String managerDN = "managerDN";
        final String managerPassword = "managerPassword";

        final String authenticationType = "digest";
        final String ldapReferral = "referral";

        final String userSearchBase = "searchbase";
        final String userSearchFilter = "searchFilter";
        final String userDNPatterns = "pattern1,pattern2";
        final String userAttributes = "attribute1,attribute2";

        final String groupSearchBase = "groupSearchBase";
        final String groupSearchFilter = "groupSearchFilter";
        final String groupRoleAttribute = "roleAttribute";
        final String rolePrefix = "ROLE_";

        final LdapProperties properties = new LdapProperties();
        properties.setEnabled(true);
        properties.setServer(server);
        properties.setManagerDN(managerDN);
        properties.setManagerPassword(managerPassword);
        properties.setAuthenticationType(authenticationType);
        properties.setLdapReferral(ldapReferral);
        properties.setUserSearchBase(userSearchBase);
        properties.setUserSearchFilter(userSearchFilter);
        properties.setUserDNPatterns(userDNPatterns);
        properties.setUserAttributes(userAttributes);
        properties.setGroupSearchBase(groupSearchBase);
        properties.setGroupSearchFilter(groupSearchFilter);
        properties.setGroupRoleAttribute(groupRoleAttribute);
        properties.setRolePrefix(rolePrefix);
        final LdapManager ldapManager = new LdapManager(properties);
        assertTrue(ldapManager.isLdapEnabled());
        properties.setEnabled(false);
        ldapManager.updateConfiguration(properties);
        assertFalse(ldapManager.isLdapEnabled());
    }

    @Test
    public void testAuthenticationProviderCreated() {
        final String server = "aserver";
        final String managerDN = "managerDN";
        final String managerPassword = "managerPassword";

        final String authenticationType = "digest";
        final String ldapReferral = "referral";

        final String userSearchBase = "searchbase";
        final String userSearchFilter = "searchFilter";
        final String userDNPatterns = "pattern1,pattern2";
        final String userAttributes = "attribute1,attribute2";

        final String groupSearchBase = "groupSearchBase";
        final String groupSearchFilter = "groupSearchFilter";
        final String groupRoleAttribute = "roleAttribute";
        final String rolePrefix = "ROLE_";

        final LdapProperties properties = new LdapProperties();
        properties.setEnabled(true);
        properties.setServer(server);
        properties.setManagerDN(managerDN);
        properties.setManagerPassword(managerPassword);
        properties.setAuthenticationType(authenticationType);
        properties.setLdapReferral(ldapReferral);
        properties.setUserSearchBase(userSearchBase);
        properties.setUserSearchFilter(userSearchFilter);
        properties.setUserDNPatterns(userDNPatterns);
        properties.setUserAttributes(userAttributes);
        properties.setGroupSearchBase(groupSearchBase);
        properties.setGroupSearchFilter(groupSearchFilter);
        properties.setGroupRoleAttribute(groupRoleAttribute);
        properties.setRolePrefix(rolePrefix);
        final LdapManager ldapManager = new LdapManager(properties);
        assertNotNull(ldapManager.getAuthenticationProvider());
    }
}
