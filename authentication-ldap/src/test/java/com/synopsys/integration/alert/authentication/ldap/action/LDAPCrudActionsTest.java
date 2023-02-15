package com.synopsys.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.synopsys.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
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

class LDAPCrudActionsTest {
    private static LDAPCrudActions ldapCrudActions;
    private static LDAPConfigModel ldapConfigModel;

    @BeforeEach
    public void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );

        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        LDAPConfigAccessor ldapConfigAccessor = new LDAPConfigAccessor(encryptionUtility, mockLDAPConfigurationRepository);

        ldapCrudActions = new LDAPCrudActions(authorizationManager, ldapConfigAccessor, new LDAPConfigurationValidator(), authenticationDescriptorKey);
        ldapConfigModel = createValidConfigModel();
    }

    @Test
    void testCreateConfig() {
        ActionResponse<LDAPConfigModel> actionResponseGetOne = ldapCrudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(ldapConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
    }

    @Test
    void testCreateConfigAlreadyExists() {
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(ldapConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseCreateAgain = ldapCrudActions.create(ldapConfigModel);
        assertFalse(actionResponseCreateAgain.isSuccessful());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponseCreateAgain.getHttpStatus());
        assertFalse(actionResponseCreateAgain.hasContent());
    }

    @Test
    void testUpdateConfigNotExist() {
        ActionResponse<LDAPConfigModel> actionResponseUpdate = ldapCrudActions.update(ldapConfigModel);
        assertFalse(actionResponseUpdate.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseUpdate.getHttpStatus());
        assertFalse(actionResponseUpdate.hasContent());
    }

    @Test
    void testUpdateConfig() {
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(ldapConfigModel);
        LDAPConfigModel createdLDAPConfigModel = actionResponseCreate.getContent().orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
        assertEquals("authenticationType", createdLDAPConfigModel.getAuthenticationType().orElse("FAIL"));

        ldapConfigModel.setAuthenticationType("UPDATE");
        ActionResponse<LDAPConfigModel> actionResponseUpdate = ldapCrudActions.update(ldapConfigModel);
        LDAPConfigModel updatedLDAPConfigModel = actionResponseUpdate.getContent().orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertTrue(actionResponseUpdate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseUpdate.getHttpStatus());
        assertTrue(actionResponseUpdate.hasContent());
        assertEquals("UPDATE", updatedLDAPConfigModel.getAuthenticationType().orElse("FAIL"));
    }

    @Test
    void testDeleteConfigNotExist() {
        ActionResponse<LDAPConfigModel> actionResponseDelete = ldapCrudActions.delete();
        assertFalse(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

    @Test
    void testDeleteConfig() {
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(ldapConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseDelete = ldapCrudActions.delete();
        assertTrue(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

    private LDAPConfigModel createValidConfigModel() {
        return new LDAPConfigModel(
            "",
            "",
            "",
            true,
            "serverName",
            "managerDn",
            "managerPassword",
            true,
            "authenticationType",
            "referral",
            "userSearchBase",
            "userSearchFilter",
            "userDnPatterns",
            "userAttributes",
            "groupSearchBase",
            "groupSearchFilter",
            "groupRoleAttributes"
        );
    }
}
