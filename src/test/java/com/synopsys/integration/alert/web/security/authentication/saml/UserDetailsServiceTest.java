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
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
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
        UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(configuration.getField(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(key))).thenReturn(List.of(configuration));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.empty());
        authoritiesPopulator = new UserManagementAuthoritiesPopulator(key, configurationAccessor, userAccessor);
    }

    @Test
    public void testValidCredential() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(VALID_ROLES);
        UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals(USER_NAME, principal.getUsername());
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertEquals(VALID_ROLES.length, principal.getAuthorities().size());
        List<String> expectedRoles = List.of(VALID_ROLES);
        List<String> actualRoles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(authority -> StringUtils.remove(authority, UserModel.ROLE_PREFIX)).collect(Collectors.toList());
        assertTrue(expectedRoles.containsAll(actualRoles));
    }

    @Test
    public void testNullRoleArray() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(null);

        UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals(USER_NAME, principal.getUsername());
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertTrue(principal.getAuthorities().isEmpty());
    }

    @Test
    public void testEmptyRoleArray() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);
        String[] roles = new String[0];
        Mockito.when(credential.getAttributeAsString("Name")).thenReturn(USER_NAME);
        Mockito.when(credential.getAttributeAsString("Email")).thenReturn(EMAIL);
        Mockito.when(credential.getAttributeAsStringArray("AlertRoles")).thenReturn(roles);

        UserDetailsService userDetailsService = new UserDetailsService(authoritiesPopulator);
        Object result = userDetailsService.loadUserBySAML(credential);

        assertNotNull(result);
        assertTrue(UserPrincipal.class.isInstance(result));
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals(USER_NAME, principal.getUsername());
        assertTrue(StringUtils.isBlank(principal.getPassword()));
        assertTrue(principal.getAuthorities().isEmpty());
    }
}
