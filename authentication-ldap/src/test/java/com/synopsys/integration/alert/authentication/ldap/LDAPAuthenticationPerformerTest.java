package com.synopsys.integration.alert.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPManager;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;

class LDAPAuthenticationPerformerTest {
    private final LDAPConfigAccessor ldapConfigAccessor = LDAPTestHelper.createTestLDAPConfigAccessor();
    private LDAPManager ldapManager;
    private LDAPAuthenticationPerformer ldapAuthenticationPerformer;
    private Authentication inputAuthentication;
    private LDAPConfigModel validaLDAPConfigModel;
    private LDAPConfigModel invalidLDAPConfigModel;

    @Mock
    UserManagementAuthoritiesPopulator mockUserManagementAuthoritiesPopulator;
    @Mock
    AuthenticationEventManager mockAuthenticationEventManager;
    @Mock
    RoleAccessor mockRoleAccessor;

    @BeforeEach
    public void init() {
        ldapManager = new LDAPManager(ldapConfigAccessor, mockUserManagementAuthoritiesPopulator, new LDAPConfig().ldapUserContextMapper());
        ldapAuthenticationPerformer = new LDAPAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, ldapManager);
        inputAuthentication = new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        validaLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
        invalidLDAPConfigModel = LDAPTestHelper.createInvalidLDAPConfigModel();
    }

    @Test
    void testGetAuthenticationType() {
        assertEquals(AuthenticationType.LDAP, ldapAuthenticationPerformer.getAuthenticationType());
    }

    @Test
    void testAuthenticateEnabledFalse() {
        validaLDAPConfigModel.setEnabled(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validaLDAPConfigModel));

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateProviderNotPresent() {
        validaLDAPConfigModel.setServerName("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validaLDAPConfigModel));

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateException() {
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validaLDAPConfigModel));

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateAlertException() {
        invalidLDAPConfigModel.setEnabled(true);
        invalidLDAPConfigModel.setServerName("serverName");
        invalidLDAPConfigModel.setManagerPassword("managerPassword");
        invalidLDAPConfigModel.setManagerDn("managerDn");
        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        Mockito.doReturn(Optional.of(invalidLDAPConfigModel)).when(spiedLDAPManager).getCurrentConfiguration();
        ldapAuthenticationPerformer = new LDAPAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, spiedLDAPManager);

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateSuccess() {
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(validaLDAPConfigModel));

        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(ldapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLDAPManager).createAuthProvider(Mockito.any(LDAPConfigModel.class)));

        // Return an authenticated token when authenticate() is called
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(spiedLdapAuthenticationProvider).authenticate(inputAuthentication);

        LDAPAuthenticationPerformer ldapAuthenticationPerformer = new LDAPAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, spiedLDAPManager);
        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertTrue(returnAuthentication.isAuthenticated());
        assertEquals("foo", returnAuthentication.getPrincipal());
    }

}
