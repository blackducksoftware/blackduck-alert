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
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.ldap.LDAPTestHelper;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.authentication.ldap.validator.LDAPConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;

class LDAPValidationActionTest {
    private static LDAPValidationAction ldapValidationAction;
    private LDAPConfigModel validLDAPConfigModel;
    private LDAPConfigModel invalidLDAPConfigModel;

    @BeforeEach
    public void init() {
        ldapValidationAction = new LDAPValidationAction(
            new LDAPConfigurationValidator(),
            LDAPTestHelper.createAuthorizationManager(),
            new AuthenticationDescriptorKey()
        );

        validLDAPConfigModel = LDAPTestHelper.createValidLDAPConfigModel();
        invalidLDAPConfigModel = LDAPTestHelper.createInvalidLDAPConfigModel();
    }

    @Test
    void testValidateInvalidConfigModel() {
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapValidationAction.validate(invalidLDAPConfigModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));

        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("There were problems with the configuration", validationResponseModel.getMessage());
    }

    @Test
    void testValidateValidConfigModel() {
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = ldapValidationAction.validate(validLDAPConfigModel);

        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));

        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertFalse(validationResponseModel.hasErrors());
        assertEquals("The configuration is valid", validationResponseModel.getMessage());
    }
}
