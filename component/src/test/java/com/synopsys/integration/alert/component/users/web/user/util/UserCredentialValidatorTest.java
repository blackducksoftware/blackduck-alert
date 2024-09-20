package com.synopsys.integration.alert.component.users.web.user.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.component.users.web.user.UserActions;

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
        assertTrue(fieldStatus.getFieldMessage().contains("at least 8 characters"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("less than 128 characters"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("at least one digit"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("at least one upper case character"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("at least one lower case character"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("at least one special character"));
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
        assertTrue(fieldStatus.getFieldMessage().contains("password is too easy to guess"));
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
