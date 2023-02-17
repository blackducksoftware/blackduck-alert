package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class LDAPTestActionTest {
    public static final String testUser = "testLDAPUsername";
    public static final String testPass = "testLDAPPassword";
    private static AuthorizationManager authorizationManager;
    private static final AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
    private static final LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();
    private static LdapManager ldapManager;
    private static LDAPTestAction ldapTestAction;
    private static LDAPConfigAccessor ldapConfigAccessor;

    private final LDAPConfigModel ldapConfigModel = new LDAPConfigModel();

    @BeforeEach
    public void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );

        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        InetOrgPersonContextMapper inetOrgPersonContextMapper = new InetOrgPersonContextMapper();
        ldapManager = new LdapManager(ldapConfigAccessor, userManagementAuthoritiesPopulator, inetOrgPersonContextMapper);

        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, ldapManager, ldapConfigAccessor);

        ldapConfigModel.setManagerDn("managerDn");
        ldapConfigModel.setManagerPassword("managerPassword");
        ldapConfigModel.setServerName("serverName");
        ldapConfigModel.setIsManagerPasswordSet(true);
        ldapConfigModel.setEnabled(false);
    }

    @Test
    void testAuthenticationValidationFails() {
        LDAPConfigModel emptyConfigModel = new LDAPConfigModel();
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(emptyConfigModel, testUser, testPass);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());

        // asserts to show we failed validation
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getErrors().size() > 0);
        assertEquals("There were problems with the configuration", validationResponseModel.getMessage());
    }

    @Test
    void testAuthenticationValidationSuccess() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(ldapConfigModel, testUser, testPass);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());

        // assert to show we passed validation
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertEquals(0, validationResponseModel.getErrors().size());

        // assert to show testConfigModelContent() caught an exception
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("LDAP Test Configuration failed. Error creating LDAP authenticator", validationResponseModel.getMessage());
    }

    @Test
    void testGetPasswordFromDB() {
        LDAPConfigModel ldapConfigModelForSpy = new LDAPConfigModel();
        ldapConfigModelForSpy.setManagerPassword("Spy Password");
        LDAPConfigAccessor spiedLDAPConfigAccessor = Mockito.spy(ldapConfigAccessor);
        Mockito.doReturn(Optional.of(ldapConfigModelForSpy)).when(spiedLDAPConfigAccessor).getConfiguration();

        ldapConfigModel.setManagerPassword("");
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(ldapConfigModel, testUser, testPass);

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, ldapManager, spiedLDAPConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        // asserts to show validation passed as it should, with password being empty
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertEquals(0, validationResponseModel.getErrors().size());

        // asserts to show testConfigModelContent() got PW from DB (spy). If it didn't get the PW from the DB
        //   the message would be different
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("LDAP Test Configuration failed. Error creating LDAP authenticator", validationResponseModel.getMessage());
    }

    @Test
    void testEmptyAuthenticationProvider() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(ldapConfigModel, testUser, testPass);
        LdapManager spiedLdapManager = Mockito.spy(ldapManager);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.empty()).when(spiedLdapManager).createAuthProvider(ldapConfigModel));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLdapManager, ldapConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        // asserts to show testConfigModelContent() got PW from DB (spy). If it didn't get the PW from the DB,
        //   the message would be different
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("LDAP Test Configuration failed. Please check your configuration.", validationResponseModel.getMessage());
    }

    @Test
    void testAuthenticateFailure() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(ldapConfigModel, testUser, testPass);
        LdapManager spiedLdapManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(mockLdapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLdapManager).createAuthProvider(ldapConfigModel));

        Authentication authentication = new UsernamePasswordAuthenticationToken("testLDAPUsername", "testLDAPPassword");
        Mockito.doReturn(authentication).when(spiedLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLdapManager, ldapConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(String.format("LDAP authentication failed for test user %s.", testUser), validationResponseModel.getMessage());
    }

    @Test
    void testAuthenticateSuccess() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(ldapConfigModel, testUser, testPass);
        LdapManager spiedLdapManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(mockLdapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLdapManager).createAuthProvider(ldapConfigModel));

        // Create an instance of an implementation of Authentication that will return true for isAuthenticated()
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(spiedLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLdapManager, ldapConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModel.hasErrors());
        assertEquals("LDAP Test Configuration successful.", validationResponseModel.getMessage());
    }

}
