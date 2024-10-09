/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.authentication.ldap.LDAPTestHelper;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;

class LDAPCrudActionsTest {
    private LDAPCrudActions ldapCrudActions;
    private LDAPConfigModel validLDAPConfigModel;

    @BeforeEach
    public void init() {
        ldapCrudActions = new LDAPCrudActions(
            LDAPTestHelper.createAuthorizationManager(),
            LDAPTestHelper.createTestLDAPConfigAccessor(),
            new LDAPConfigurationValidator(),
            new AuthenticationDescriptorKey()
        );

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
    }

    @Test
    void testCreateConfig() {
        ActionResponse<LDAPConfigModel> actionResponseGetOne = ldapCrudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(validLDAPConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
    }

    @Test
    void testCreateConfigAlreadyExists() {
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(validLDAPConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseCreateAgain = ldapCrudActions.create(validLDAPConfigModel);
        assertFalse(actionResponseCreateAgain.isSuccessful());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponseCreateAgain.getHttpStatus());
        assertFalse(actionResponseCreateAgain.hasContent());
    }

    @Test
    void testUpdateConfigNotExist() {
        ActionResponse<LDAPConfigModel> actionResponseUpdate = ldapCrudActions.update(validLDAPConfigModel);
        assertFalse(actionResponseUpdate.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseUpdate.getHttpStatus());
        assertFalse(actionResponseUpdate.hasContent());
    }

    @Test
    void testUpdateConfig() {
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(validLDAPConfigModel);
        LDAPConfigModel createdLDAPConfigModel = actionResponseCreate.getContent().orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
        LDAPTestHelper.assertOptionalField(validLDAPConfigModel::getAuthenticationType, createdLDAPConfigModel::getAuthenticationType);

        validLDAPConfigModel.setAuthenticationType(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST);
        ActionResponse<LDAPConfigModel> actionResponseUpdate = ldapCrudActions.update(validLDAPConfigModel);
        LDAPConfigModel updatedLDAPConfigModel = actionResponseUpdate.getContent().orElseThrow(() -> new AssertionFailedError("Updated LDAPConfigModel did not exist"));
        assertTrue(actionResponseUpdate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseUpdate.getHttpStatus());
        assertTrue(actionResponseUpdate.hasContent());
        LDAPTestHelper.assertOptionalField(LDAPTestHelper.DEFAULT_AUTH_TYPE_DIGEST, updatedLDAPConfigModel::getAuthenticationType);
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
        ActionResponse<LDAPConfigModel> actionResponseCreate = ldapCrudActions.create(validLDAPConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<LDAPConfigModel> actionResponseDelete = ldapCrudActions.delete();
        assertTrue(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

}
