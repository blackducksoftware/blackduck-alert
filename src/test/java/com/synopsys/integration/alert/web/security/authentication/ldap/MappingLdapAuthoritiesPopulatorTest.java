package com.synopsys.integration.alert.web.security.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;

import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;

public class MappingLdapAuthoritiesPopulatorTest {

    @Test
    public void testEmptyGroupSearchBase() {
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ContextSource contextSource = Mockito.mock(ContextSource.class);
        MappingLdapAuthoritiesPopulator ldapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, null, authoritiesPopulator);
        Set<GrantedAuthority> actualRoles = ldapAuthoritiesPopulator.getGroupMembershipRoles("", "");
        assertTrue(actualRoles.isEmpty());
    }

    @Test
    public void testEmptyAdditionalRoles() {
        UserManagementAuthoritiesPopulator authoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ContextSource contextSource = Mockito.mock(ContextSource.class);
        DirContextOperations user = Mockito.mock(DirContextOperations.class);
        MappingLdapAuthoritiesPopulator ldapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, null, authoritiesPopulator);
        Set<GrantedAuthority> actualRoles = ldapAuthoritiesPopulator.getAdditionalRoles(user, "");
        assertTrue(actualRoles.isEmpty());
    }

}
