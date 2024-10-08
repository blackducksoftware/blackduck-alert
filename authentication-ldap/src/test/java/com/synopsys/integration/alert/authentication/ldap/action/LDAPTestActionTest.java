package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.LDAPConfig;
import com.synopsys.integration.alert.authentication.ldap.LDAPTestHelper;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

class LDAPTestActionTest {
    public static final String testUser = "testLDAPUsername";
    public static final String testPass = "testLDAPPassword";
    private static final AuthorizationManager authorizationManager = LDAPTestHelper.createAuthorizationManager();

    private static final AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
    private static final LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

    private static LDAPConfigAccessor ldapConfigAccessor;
    private static LDAPManager ldapManager;
    private static LDAPTestAction ldapTestAction;

    private LDAPConfigModel validLDAPConfigModel;
    private LDAPConfigModel invalidLDAPConfigModel;

    @BeforeEach
    public void init() {
        ldapConfigAccessor = LDAPTestHelper.createTestLDAPConfigAccessor();

        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ldapManager = new LDAPManager(ldapConfigAccessor, userManagementAuthoritiesPopulator, new LDAPConfig().ldapUserContextMapper());

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, ldapManager, ldapConfigAccessor);

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
        validLDAPConfigModel.setEnabled(false);

        invalidLDAPConfigModel = LDAPTestHelper.createInvalidLDAPConfigModel();
    }

    @Test
    void testAuthenticationValidationFails() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(invalidLDAPConfigModel, testUser, testPass);
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
        validLDAPConfigModel.setUserSearchFilter("");
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, testUser, testPass);
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
        invalidLDAPConfigModel.setManagerPassword("Spy Password");
        LDAPConfigAccessor spiedLDAPConfigAccessor = Mockito.spy(ldapConfigAccessor);
        Mockito.doReturn(Optional.of(invalidLDAPConfigModel)).when(spiedLDAPConfigAccessor).getConfiguration();

        validLDAPConfigModel.setUserSearchFilter("");
        validLDAPConfigModel.setManagerPassword("");
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, testUser, testPass);

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
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, testUser, testPass);
        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.empty()).when(spiedLDAPManager).createAuthProvider(validLDAPConfigModel));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLDAPManager, ldapConfigAccessor);
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
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, testUser, testPass);
        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(mockLdapAuthenticationProvider)).when(spiedLDAPManager).createAuthProvider(validLDAPConfigModel));

        Authentication authentication = new UsernamePasswordAuthenticationToken("testLDAPUsername", "testLDAPPassword");
        Mockito.doReturn(authentication).when(mockLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLDAPManager, ldapConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(String.format("LDAP authentication failed for test user %s.", testUser), validationResponseModel.getMessage());
    }

    @Test
    void testAuthenticateSuccess() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, testUser, testPass);
        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(mockLdapAuthenticationProvider)).when(spiedLDAPManager).createAuthProvider(validLDAPConfigModel));

        // Create an instance of an implementation of Authentication that will return true for isAuthenticated()
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(mockLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLDAPManager, ldapConfigAccessor);
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapTestAction.testAuthentication(ldapConfigTestModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModel.hasErrors());
        assertEquals("LDAP Test Configuration successful.", validationResponseModel.getMessage());
    }

    @Test
    void testConfigModelContentInvalidConfig() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(invalidLDAPConfigModel, testUser, testPass);
        ConfigurationTestResult configurationTestResult = ldapTestAction.testConfigModelContent(ldapConfigTestModel);

        assertFalse(configurationTestResult.isSuccess());
        assertEquals("LDAP Test Configuration failed. Please check your configuration.", configurationTestResult.getStatusMessage());
    }

}
