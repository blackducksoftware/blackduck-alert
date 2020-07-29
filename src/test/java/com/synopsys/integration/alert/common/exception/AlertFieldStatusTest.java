package com.synopsys.integration.alert.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AlertFieldStatusTest {

    private final String FIELD_ERROR_MESSAGE = "Test Field Error Message";

    @Test
    public void getSeverityTest() {
        AlertFieldStatus alertFieldStatusError = new AlertFieldStatus(FieldErrorSeverity.ERROR, FIELD_ERROR_MESSAGE);
        AlertFieldStatus alertFieldStatusWarning = new AlertFieldStatus(FieldErrorSeverity.WARNING, FIELD_ERROR_MESSAGE);

        assertEquals(FieldErrorSeverity.ERROR, alertFieldStatusError.getSeverity());
        assertEquals(FieldErrorSeverity.WARNING, alertFieldStatusWarning.getSeverity());
    }

    @Test
    public void getFieldErrorMessage() {
        AlertFieldStatus alertFieldStatus = new AlertFieldStatus(FieldErrorSeverity.ERROR, FIELD_ERROR_MESSAGE);

        assertEquals(FIELD_ERROR_MESSAGE, alertFieldStatus.getFieldMessage());
    }
}
