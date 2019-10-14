package com.synopsys.integration.alert.web.security.authentication.saml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SAMLCredential;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.web.security.authentication.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.web.security.authentication.database.UserPrincipal;

public class UserDetailsServiceTest {

    private static final String USER_NAME = "user_name";
    private static final String EMAIL = "email_address";
    private static final String[] VALID_ROLES = { "ALERT_ADMIN" };
    private UserManagementAuthoritiesPopulator authoritiesPopulator;

    @BeforeEach
    public void initializeAuthoritiesPopulator() throws Exception {
        AuthenticationDescriptorKey key = new AuthenticationDescriptorKey();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationModel configuration = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configuration.getField(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(key))).thenReturn(List.of(configuration));
        authoritiesPopulator = new UserManagementAuthoritiesPopulator(key, configurationAccessor);
    }

    @Test
    public void testValidCredential() {
        final SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(VALID_ROLES);
        final UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        final Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        final UserPrincipal principal = (UserPrincipal) result;
        assertEquals(principal.getUsername(), USER_NAME);
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertEquals(VALID_ROLES.length, principal.getAuthorities().size());
        final List<String> expectedRoles = List.of(VALID_ROLES);
        final List<String> actualRoles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(authority -> StringUtils.remove(authority, UserModel.ROLE_PREFIX)).collect(Collectors.toList());
        assertTrue(expectedRoles.containsAll(actualRoles));
    }

    @Test
    public void testNullRoleArray() {
        final SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(null);

        final UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        final Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        final UserPrincipal principal = (UserPrincipal) result;
        assertEquals(principal.getUsername(), USER_NAME);
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertTrue(principal.getAuthorities().isEmpty());
    }

    @Test
    public void testEmptyRoleArray() {
        final SAMLCredential credential = Mockito.mock(SAMLCredential.class);
        final String[] roles = new String[0];
        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(roles);

        final UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        final Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        final UserPrincipal principal = (UserPrincipal) result;
        assertEquals(principal.getUsername(), USER_NAME);
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertTrue(principal.getAuthorities().isEmpty());
    }
}
