/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.users.web.user.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.FieldStatusSeverity;
import com.blackduck.integration.alert.component.users.web.user.UserActions;

class UserCredentialValidatorTest {
    private final UserCredentialValidator validator = new UserCredentialValidator();

    @Test
    void shortPasswordLengthTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "short";
        validator.validatePasswordLength(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_TOO_SHORT_ERROR_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void longPasswordLengthTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "a".repeat(129);
        validator.validatePasswordLength(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_TOO_LONG_ERROR_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void passwordLengthTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "TestPassword123";
        validator.validatePasswordLength(fieldErrors, password);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    void missingDigitPasswordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "aA@";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_NO_DIGIT_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void missingUppercasePasswordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "a@1";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_NO_UPPER_CASE_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void missingLowercasePasswordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "A@1";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_NO_LOWER_CASE_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void missingSpecialCharacterPasswordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "aA1";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_NO_SPECIAL_CHARACTER_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void missingMultiplePasswordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "lowercaseonlypassword";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(3, fieldErrors.size());
    }

    @Test
    void passwordComplexityTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "A valid password containing numbers, UPPERCASE, Digits 123, and special characters !@#$%.";
        validator.validatePasswordComplexity(fieldErrors, password);
        assertEquals(0, fieldErrors.size());
    }

    @Test
    void invalidCommonPasswordTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "hunter1";
        validator.validateCommonPasswords(fieldErrors, password);
        assertEquals(1, fieldErrors.size());
        AlertFieldStatus fieldStatus = fieldErrors.stream().findFirst().orElseThrow(() -> new AssertionError("Expected field error but none were found"));
        assertEquals(UserActions.FIELD_KEY_USER_MGMT_PASSWORD, fieldStatus.getFieldName());
        assertTrue(fieldStatus.getFieldMessage().contains(UserCredentialValidator.PASSWORD_TOO_SIMPLE_MESSAGE));
        assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
    }

    @Test
    void commonPasswordTest() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        String password = "uniquePasswordNotInTheTop10KPasswords";
        validator.validateCommonPasswords(fieldErrors, password);
        assertEquals(0, fieldErrors.size());
    }
}
