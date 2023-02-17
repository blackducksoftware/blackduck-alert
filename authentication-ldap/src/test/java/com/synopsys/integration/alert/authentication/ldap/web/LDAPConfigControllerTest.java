package com.synopsys.integration.alert.authentication.ldap.web;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
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
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.authentication.security.UserManagementAuthoritiesPopulator;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.LdapConfig;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPCrudActions;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPTestAction;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPValidationAction;
import com.synopsys.integration.alert.authentication.ldap.action.LdapManager;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class LDAPConfigControllerTest {
    private static final AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
    private static final LDAPConfigurationValidator ldapConfigurationValidator = new LDAPConfigurationValidator();

    private static AuthorizationManager authorizationManager;
    private static LdapManager ldapManager;
    private static LDAPConfigAccessor ldapConfigAccessor;
    private static LDAPValidationAction ldapValidationAction;
    private static LDAPCrudActions ldapCrudActions;
    private static LDAPConfigController ldapConfigController;

    private static LDAPConfigModel invalidLDAPConfigModel;
    private static LDAPConfigModel validLDAPConfigModel;

    @BeforeEach
    public void initAccessor() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );

        InetOrgPersonContextMapper inetOrgPersonContextMapper = new LdapConfig().ldapUserContextMapper();
        UserManagementAuthoritiesPopulator userManagementAuthoritiesPopulator = Mockito.mock(UserManagementAuthoritiesPopulator.class);
        ldapManager = new LdapManager(ldapConfigAccessor, userManagementAuthoritiesPopulator, inetOrgPersonContextMapper);

        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);

        LDAPTestAction ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, ldapManager, ldapConfigAccessor);
        ldapValidationAction = new LDAPValidationAction(ldapConfigurationValidator, authorizationManager, authenticationDescriptorKey);
        ldapCrudActions = new LDAPCrudActions(authorizationManager, ldapConfigAccessor, ldapConfigurationValidator, authenticationDescriptorKey);
        ldapConfigController = new LDAPConfigController(ldapCrudActions, ldapValidationAction, ldapTestAction);

        invalidLDAPConfigModel = createValidConfigModel();
        invalidLDAPConfigModel.setServerName("");

        validLDAPConfigModel = createValidConfigModel();
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
        LDAPConfigModel createdLDAPConfigModel = assertDoesNotThrow(() -> ldapConfigController.create(validLDAPConfigModel));
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
        LdapManager spiedLdapManager = Mockito.spy(ldapManager);
        LdapAuthenticationProvider mockLdapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        LdapAuthenticationProvider spiedLdapAuthenticationProvider = Mockito.spy(mockLdapAuthenticationProvider);
        assertDoesNotThrow(() -> Mockito.doReturn(Optional.of(spiedLdapAuthenticationProvider)).when(spiedLdapManager).createAuthProvider(validLDAPConfigModel));

        // Create an instance of an implementation of Authentication that will return true for isAuthenticated()
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
        TestingAuthenticationToken authenticatedToken = new TestingAuthenticationToken("foo", "bar", List.of(simpleGrantedAuthority));
        Mockito.doReturn(authenticatedToken).when(spiedLdapAuthenticationProvider).authenticate(Mockito.any(Authentication.class));

        LDAPTestAction ldapTestAction = new LDAPTestAction(authorizationManager, authenticationDescriptorKey, ldapConfigurationValidator, spiedLdapManager, ldapConfigAccessor);
        LDAPConfigController ldapConfigController = new LDAPConfigController(ldapCrudActions, ldapValidationAction, ldapTestAction);

        LDAPConfigTestModel ldapConfigTestModel = new LDAPConfigTestModel(validLDAPConfigModel, "User", "Pass");
        ValidationResponseModel validationResponseModel = assertDoesNotThrow(() -> ldapConfigController.test(ldapConfigTestModel));
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
