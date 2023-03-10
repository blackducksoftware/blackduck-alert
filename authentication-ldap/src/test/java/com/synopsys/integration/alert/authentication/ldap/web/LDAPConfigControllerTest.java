package com.synopsys.integration.alert.authentication.ldap.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.LDAPConfig;
import com.synopsys.integration.alert.authentication.ldap.LDAPTestHelper;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPCrudActions;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPManager;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPTestAction;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPValidationAction;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

class LDAPConfigControllerTest {
    private static final AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
    private static final LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();
    private static final AuthorizationManager authorizationManager = LDAPTestHelper.createAuthorizationManager();

    private static LDAPManager ldapManager;
    private static LDAPConfigAccessor ldapConfigAccessor;
    private static LDAPValidationAction ldapValidationAction;
    private static LDAPCrudActions ldapCrudActions;
    private static LDAPConfigController ldapConfigController;

    private LDAPConfigModel invalidLDAPConfigModel;
    private LDAPConfigModel validLDAPConfigModel;

    @BeforeEach
    public void initAccessor() {
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ldapManager = new LDAPManager(ldapConfigAccessor, userManagementAuthoritiesPopulator, new LDAPConfig().ldapUserContextMapper());

        ldapConfigAccessor = LDAPTestHelper.createTestLDAPConfigAccessor();

        LDAPTestAction ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, ldapManager, ldapConfigAccessor);
        ldapValidationAction = new LDAPValidationAction(ldapConfigurationValidator, authorizationManager, authenticationDescriptorKey);
        ldapCrudActions = new LDAPCrudActions(authorizationManager, ldapConfigAccessor, ldapConfigurationValidator, authenticationDescriptorKey);
        ldapConfigController = new LDAPConfigController(ldapCrudActions, ldapValidationAction, ldapTestAction);

        invalidLDAPConfigModel = LDAPTestHelper.createInvalidLDAPConfigModel();

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
    }

    @Test
    void testGetOneNotExist() {
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.getOne());
        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
    }

    @Test
    void testDeleteNotExist() {
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.delete());
        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
    }

    @Test
    void testCreateGetDeleteValidModel() {
        validLDAPConfigModel.setId("");
        LDAPConfigModel createdLDAPConfigModel = assertDoesNotThrow(() -> ldapConfigController.create(validLDAPConfigModel));
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_MANAGER_PASSWORD, validLDAPConfigModel::getManagerPassword);
        assertTrue(validLDAPConfigModel.getManagerPassword().isPresent());
        assertFalse(createdLDAPConfigModel.getManagerPassword().isPresent());
        assertTrue(StringUtils.isBlank(validLDAPConfigModel.getId()));
        assertTrue(StringUtils.isNotBlank(createdLDAPConfigModel.getId()));

        LDAPConfigModel retrievedLDAPConfigModel = assertDoesNotThrow(() -> ldapConfigController.getOne());
        assertFalse(retrievedLDAPConfigModel.getManagerPassword().isPresent());
        assertEquals(createdLDAPConfigModel.getId(), retrievedLDAPConfigModel.getId());

        assertDoesNotThrow(() -> ldapConfigController.delete());

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.getOne());
        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
    }

    @Test
    void testCreateInvalidModel() {
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.create(invalidLDAPConfigModel));
        assertEquals("There were problems with the configuration", responseStatusException.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
    }

    @Test
    void testCreateAlreadyExists() {
        assertDoesNotThrow(() -> ldapConfigController.create(validLDAPConfigModel));
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.create(validLDAPConfigModel));
        assertEquals("A configuration with this name already exists.", responseStatusException.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
    }

    @Test
    void testUpdateNotExist() {
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.update(validLDAPConfigModel));
        assertEquals(HttpStatus.NOT_FOUND, responseStatusException.getStatus());
    }

    @Test
    void testUpdateInvalidModel() {
        assertDoesNotThrow(() -> ldapConfigController.create(validLDAPConfigModel));

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> ldapConfigController.update(invalidLDAPConfigModel));
        assertEquals("There were problems with the configuration", responseStatusException.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
    }

    @Test
    void testUpdateValidModel() {
        LDAPConfigModel createdLDAPConfigModel = assertDoesNotThrow(() -> ldapConfigController.create(validLDAPConfigModel));
        assertEquals(validLDAPConfigModel.getServerName(), createdLDAPConfigModel.getServerName());

        validLDAPConfigModel.setServerName("Updated ServerName");
        assertDoesNotThrow(() -> ldapConfigController.update(validLDAPConfigModel));

        LDAPConfigModel retrievedLDAPConfigModel = assertDoesNotThrow(() -> ldapConfigController.getOne());
        assertEquals("Updated ServerName", retrievedLDAPConfigModel.getServerName());
    }

    @Test
    void testValidateInvalidModel() {
        ValidationResponseModel validationResponseModel = assertDoesNotThrow(() -> ldapConfigController.validate(invalidLDAPConfigModel));
        assertEquals("There were problems with the configuration", validationResponseModel.getMessage());
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void testValidateValidModel() {
        ValidationResponseModel validationResponseModel = assertDoesNotThrow(() -> ldapConfigController.validate(validLDAPConfigModel));
        assertEquals("The configuration is valid", validationResponseModel.getMessage());
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void testTestInvalidModel() {
        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(invalidLDAPConfigModel, "User", "Pass");
        ValidationResponseModel validationResponseModel = assertDoesNotThrow(() -> ldapConfigController.test(ldapConfigTestModel));
        assertEquals("There were problems with the configuration", validationResponseModel.getMessage());
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void testTestValidModel() {
        LDAPManager spiedLDAPManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(mockLdapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLDAPManager).createAuthProvider(validLDAPConfigModel));

        // Create an instance of an implementation of Authentication that will return true for isAuthenticated()
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(spiedLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        LDAPTestAction ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLDAPManager, ldapConfigAccessor);
        LDAPConfigController ldapConfigController = new LDAPConfigController(ldapCrudActions, ldapValidationAction, ldapTestAction);

        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, "User", "Pass");
        ValidationResponseModel validationResponseModel = assertDoesNotThrow(() -> ldapConfigController.test(ldapConfigTestModel));
        assertEquals("LDAP Test Configuration successful.", validationResponseModel.getMessage());
        assertFalse(validationResponseModel.hasErrors());
    }

}
