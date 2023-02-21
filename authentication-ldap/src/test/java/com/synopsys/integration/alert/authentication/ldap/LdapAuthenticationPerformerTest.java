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
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.authentication.ldap.action.LdapManager;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class LdapAuthenticationPerformerTest {
    private LDAPConfigAccessor ldapConfigAccessor;
    private LdapManager ldapManager;
    private LdapAuthenticationPerformer ldapAuthenticationPerformer;
    private Authentication inputAuthentication;
    private static LDAPConfigModel ldapConfigModel;

    @Mock
    UserManagementAuthoritiesPopulator MockUserManagementAuthoritiesPopulator;
    @Mock
    AuthenticationEventManager mockAuthenticationEventManager;
    @Mock
    RoleAccessor mockRoleAccessor;
    @Mock
    LdapAuthenticationProvider mockLdapAuthenticationProvider;

    @BeforeEach
    public void init() {
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        InetOrgPersonContextMapper inetOrgPersonContextMapper = new LdapConfig().ldapUserContextMapper();

        ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);
        ldapManager = new LdapManager(ldapConfigAccessor, MockUserManagementAuthoritiesPopulator, inetOrgPersonContextMapper);
        ldapAuthenticationPerformer = new LdapAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, ldapManager);
        inputAuthentication = new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        ldapConfigModel = createValidConfigModel();
    }

    @Test
    void testGetAuthenticationType() {
        assertEquals(AuthenticationType.LDAP, ldapAuthenticationPerformer.getAuthenticationType());
    }

    @Test
    void testAuthenticateEnabledFalse() {
        ldapConfigModel.setEnabled(false);
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateProviderNotPresent() {
        ldapConfigModel.setServerName("");
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));

        LdapManager spiedLDAPManager = Mockito.spy(ldapManager);
        ldapAuthenticationPerformer = new LdapAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, spiedLDAPManager);

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateException() {
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateAlertException() {
        LDAPConfigModel emptyLDAPConfigModel = new LDAPConfigModel();
        emptyLDAPConfigModel.setEnabled(true);
        emptyLDAPConfigModel.setServerName("serverName");
        emptyLDAPConfigModel.setManagerPassword("managerPassword");
        emptyLDAPConfigModel.setManagerDn("managerDn");
        LdapManager spiedLDAPManager = Mockito.spy(ldapManager);
        Mockito.doReturn(Optional.of(emptyLDAPConfigModel)).when(spiedLDAPManager).getCurrentConfiguration();
        ldapAuthenticationPerformer = new LdapAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, spiedLDAPManager);

        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertEquals(inputAuthentication.getPrincipal(), returnAuthentication.getPrincipal());
    }

    @Test
    void testAuthenticateSuccess() {
        assertDoesNotThrow(() -> ldapConfigAccessor.createConfiguration(ldapConfigModel));

        LdapManager spiedLDAPManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(ldapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLDAPManager).createAuthProvider(Mockito.any(LDAPConfigModel.class)));

        // Return an authenticated token when authenticate() is called
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(spiedLdapAuthenticationProvider).authenticate(inputAuthentication);

        LdapAuthenticationPerformer ldapAuthenticationPerformer = new LdapAuthenticationPerformer(mockAuthenticationEventManager, mockRoleAccessor, spiedLDAPManager);
        Authentication returnAuthentication = ldapAuthenticationPerformer.authenticateWithProvider(inputAuthentication);
        assertTrue(returnAuthentication.isAuthenticated());
        assertEquals("foo", returnAuthentication.getPrincipal());
    }

    private LDAPConfigModel createValidConfigModel() {
        String dateStamp = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        return new LDAPConfigModel(
            "",
            dateStamp,
            dateStamp,
            true,
            "ldap://alert.blackduck.synopsys.com:389",
            "cn=Alert Manager,ou=Synopsys,ou=people,dc=blackduck,dc=com",
            "managerPassword",
            true,
            "Simple",
            "",
            "ou=people,dc=blackduck,dc=com",
            "cn={0}",
            "",
            "",
            "ou=groups,dc=blackduck,dc=com",
            "uniqueMember={0}",
            "cn"
        );
    }

}
