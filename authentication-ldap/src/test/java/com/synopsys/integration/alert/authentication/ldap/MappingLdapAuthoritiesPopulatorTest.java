package com.synopsys.integration.alert.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.mockito.Mockito;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;

public class MappingLdapAuthoritiesPopulatorTest {

    @Test
    public void testEmptyGroupSearchBase() {
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ContextSource contextSource = Mockito.mock(ContextSource.class);
        MappingLdapAuthoritiesPopulator mappingLdapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, null, userManagementAuthoritiesPopulator);
        Set<GrantedAuthority> actualRoles = mappingLdapAuthoritiesPopulator.getGroupMembershipRoles("", "");
        assertTrue(actualRoles.isEmpty());
    }

    @Test
    public void testExceptionGroupMembershipRoles() {
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ContextSource contextSource = Mockito.mock(ContextSource.class);
        SpringSecurityLdapTemplate ldapTemplate = new SpringSecurityLdapTemplate(contextSource) {
            @Override
            public Set<Map<String, List<String>>> searchForMultipleAttributeValues(String base, String filter, Object[] params, String[] attributeNames) {
                throw new JUnitException("Group Membership Roles Test Exception");
            }
        };
        MappingLdapAuthoritiesPopulator mappingLdapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, "searchbase={0}", userManagementAuthoritiesPopulator) {
            @Override
            protected SpringSecurityLdapTemplate getLdapTemplate() {
                return ldapTemplate;
            }
        };
        Set<GrantedAuthority> actualRoles = mappingLdapAuthoritiesPopulator.getGroupMembershipRoles(null, null);
        assertTrue(actualRoles.isEmpty());
    }

    @Test
    public void testEmptyAdditionalRoles() {
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        Mockito.doReturn(Set.of(new SimpleGrantedAuthority(DefaultUserRole.ALERT_USER.name())))
            .when(userManagementAuthoritiesPopulator).addAdditionalRoles(Mockito.anyString(), Mockito.anySet());
        ContextSource contextSource = Mockito.mock(ContextSource.class);
        DirContextOperations user = Mockito.mock(DirContextOperations.class);
        MappingLdapAuthoritiesPopulator mappingLdapAuthoritiesPopulator = new MappingLdapAuthoritiesPopulator(contextSource, null, userManagementAuthoritiesPopulator);
        Set<GrantedAuthority> actualRoles = mappingLdapAuthoritiesPopulator.getAdditionalRoles(user, "");
        boolean hasAlertUserRole = actualRoles.stream()
            .map(GrantedAuthority::getAuthority)
            .allMatch(roleName -> DefaultUserRole.ALERT_USER.name().equals(roleName));

        Mockito.verify(userManagementAuthoritiesPopulator).addAdditionalRoles(Mockito.anyString(), Mockito.anySet());
        assertFalse(actualRoles.isEmpty());
        assertTrue(hasAlertUserRole);
    }

}
