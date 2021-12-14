package com.synopsys.integration.alert.component.authentication.security.saml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SAMLCredential;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.UserPrincipal;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.component.authentication.security.UserManagementAuthoritiesPopulator;

public class UserDetailsServiceTest {

    private static final String USER_NAME = "user_name";
    private static final String EMAIL = "email_address";
    private static final String[] VALID_ROLES = { "ALERT_ADMIN" };
    private static final String[] VALID_DB_ROLES = { "ALERT_USER" };
    private UserManagementAuthoritiesPopulator authoritiesPopulator;

    @BeforeEach
    public void initializeAuthoritiesPopulator() {
        Set<UserRoleModel> roles = Arrays.stream(VALID_DB_ROLES)
                                       .map(UserRoleModel::of)
                                       .collect(Collectors.toSet());
        UserModel userModel = UserModel.newUser(USER_NAME, "password", EMAIL, AuthenticationType.SAML, roles, true);
        AuthenticationDescriptorKey key = new AuthenticationDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationModel configuration = Mockito.mock(ConfigurationModel.class);
        UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(configuration.getField(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(Mockito.eq(key))).thenReturn(List.of(configuration));
        Mockito.when(userAccessor.getUser(Mockito.anyString())).thenReturn(Optional.of(userModel));
        authoritiesPopulator = new UserManagementAuthoritiesPopulator(key, configurationModelConfigurationAccessor, userAccessor);
    }

    @Test
    public void testValidCredential() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        NameID nameId = Mockito.mock(NameID.class);
        Mockito.when(nameId.getValue()).thenReturn(USER_NAME);
        Mockito.when(credential.getNameID()).thenReturn(nameId);
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
        assertEquals(VALID_ROLES.length + VALID_DB_ROLES.length, principal.getAuthorities().size());
        List<String> expectedRoles = new ArrayList<>();
        expectedRoles.addAll(Arrays.asList(VALID_ROLES));
        expectedRoles.addAll(Arrays.asList(VALID_DB_ROLES));
        List<String> actualRoles = extractRoleNamesFromPrincipal(principal);
        assertTrue(expectedRoles.containsAll(actualRoles));
    }

    @Test
    public void testNullRoleArray() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);

        NameID nameId = Mockito.mock(NameID.class);
        Mockito.when(nameId.getValue()).thenReturn(USER_NAME);
        Mockito.when(credential.getNameID()).thenReturn(nameId);
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
        assertEquals(VALID_DB_ROLES.length, principal.getAuthorities().size());
        List<String> expectedRoles = Arrays.asList(VALID_DB_ROLES);
        List<String> actualRoles = extractRoleNamesFromPrincipal(principal);
        assertTrue(expectedRoles.containsAll(actualRoles));
    }

    @Test
    public void testEmptyRoleArray() {
        SAMLCredential credential = Mockito.mock(SAMLCredential.class);
        String[] roles = new String[0];
        NameID nameId = Mockito.mock(NameID.class);
        Mockito.when(nameId.getValue()).thenReturn(USER_NAME);
        Mockito.when(credential.getNameID()).thenReturn(nameId);
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
        assertEquals(VALID_DB_ROLES.length, principal.getAuthorities().size());
        List<String> expectedRoles = Arrays.asList(VALID_DB_ROLES);
        List<String> actualRoles = extractRoleNamesFromPrincipal(principal);
        assertTrue(expectedRoles.containsAll(actualRoles));
    }

    private List<String> extractRoleNamesFromPrincipal(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                   .map(GrantedAuthority::getAuthority)
                   .map(authority -> StringUtils.remove(authority, UserModel.ROLE_PREFIX))
                   .collect(Collectors.toList());
    }

}
