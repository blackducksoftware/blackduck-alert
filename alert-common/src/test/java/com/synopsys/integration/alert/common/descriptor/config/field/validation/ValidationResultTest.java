package com.synopsys.integration.alert.common.descriptor.config.field.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class ValidationResultTest {
    private final String ERROR_MESSAGE_1 = "Error Message 1";
    private final String ERROR_MESSAGE_2 = "Error Message 2";
    private final String WARNING_MESSAGE_1 = "Warning Message 1";
    private final String WARNING_MESSAGE_2 = "Warning Message 2";

    @Test
    public void getErrorsTest() {
        ValidationResult errors = ValidationResult.errors(ERROR_MESSAGE_1);
        ArrayList<String> errorList = new ArrayList<>(errors.getErrors());

        assertEquals(1, errorList.size());
        assertEquals(ERROR_MESSAGE_1, errorList.get(0));
    }

    @Test
    public void getWarningsTest() {
        ValidationResult warnings = ValidationResult.warnings(WARNING_MESSAGE_1);
        ArrayList<String> warningList = new ArrayList<>(warnings.getWarnings());

        assertEquals(1, warningList.size());
        assertEquals(WARNING_MESSAGE_1, warningList.get(0));
    }

    @Test
    public void hasErrorsTest() {
        ValidationResult errors = ValidationResult.errors(ERROR_MESSAGE_1);
        ValidationResult emptyErrors = ValidationResult.success();

        assertTrue(errors.hasErrors());
        assertFalse(emptyErrors.hasErrors());
    }

    @Test
    public void hasWarningsTest() {
        ValidationResult warnings = ValidationResult.warnings(WARNING_MESSAGE_1);
        ValidationResult emptyWarnings = ValidationResult.success();

        assertTrue(warnings.hasWarnings());
        assertFalse(emptyWarnings.hasErrors());
    }

    @Test
    public void combineErrorMessages() {
        List<String> listOfErrorStrings = List.of(ERROR_MESSAGE_1, ERROR_MESSAGE_2);
        ValidationResult errors = ValidationResult.errors(listOfErrorStrings);

        String expectedString = StringUtils.join(listOfErrorStrings, ", ");

        assertEquals(2, errors.getErrors().size());
        assertEquals(expectedString, errors.combineErrorMessages());
    }

    @Test
    public void combineWarningMessages() {
        List<String> listOfWarningsStrings = List.of(WARNING_MESSAGE_1, WARNING_MESSAGE_2);
        ValidationResult warnings = ValidationResult.warnings(listOfWarningsStrings);

        String expectedString = StringUtils.join(listOfWarningsStrings, ", ");

        assertEquals(2, warnings.getWarnings().size());
        assertEquals(expectedString, warnings.combineWarningMessages());
    }

    @Test
    public void ofValidationResultTest() {
        ValidationResult error1 = ValidationResult.errors(ERROR_MESSAGE_1);
        ValidationResult error2 = ValidationResult.errors(ERROR_MESSAGE_2);
        ValidationResult warning1 = ValidationResult.warnings(WARNING_MESSAGE_1);
        ValidationResult warning2 = ValidationResult.warnings(WARNING_MESSAGE_2);

        ValidationResult combinedErrorsAndWarnings = ValidationResult.of(error1, error2, warning1, warning2);
        Collection<String> errorList = combinedErrorsAndWarnings.getErrors();
        Collection<String> warningList = combinedErrorsAndWarnings.getWarnings();

        assertEquals(2, errorList.size());
        assertEquals(2, warningList.size());
        assertTrue(errorList.contains(ERROR_MESSAGE_1));
        assertTrue(errorList.contains(ERROR_MESSAGE_2));
        assertTrue(warningList.contains(WARNING_MESSAGE_1));
        assertTrue(warningList.contains(WARNING_MESSAGE_2));
    }

}
