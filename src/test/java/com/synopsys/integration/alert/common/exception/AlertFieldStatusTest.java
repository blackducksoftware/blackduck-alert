package com.synopsys.integration.alert.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;

public class AlertFieldStatusTest {
    private final String FIELD_NAME = "FieldName";
    private final String FIELD_ERROR_MESSAGE = "Test Field Error Message";
    private final String FIELD_WARNING_MESSAGE = "Test Field Warning Message";

    @Test
    public void getFieldNameTest() {
        AlertFieldStatus alertFieldStatusError = AlertFieldStatus.error(FIELD_NAME, FIELD_ERROR_MESSAGE);
        AlertFieldStatus alertFieldStatusWarning = AlertFieldStatus.warning(FIELD_NAME, FIELD_WARNING_MESSAGE);

        assertEquals(FIELD_NAME, alertFieldStatusError.getFieldName());
        assertEquals(FIELD_NAME, alertFieldStatusWarning.getFieldName());
    }

    @Test
    public void getSeverityTest() {
        AlertFieldStatus alertFieldStatusError = AlertFieldStatus.error(FIELD_NAME, FIELD_ERROR_MESSAGE);
        AlertFieldStatus alertFieldStatusWarning = AlertFieldStatus.warning(FIELD_NAME, FIELD_WARNING_MESSAGE);

        assertEquals(FieldStatusSeverity.ERROR, alertFieldStatusError.getSeverity());
        assertEquals(FieldStatusSeverity.WARNING, alertFieldStatusWarning.getSeverity());
    }

    @Test
    public void getFieldErrorMessage() {
        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error(FIELD_NAME, FIELD_ERROR_MESSAGE);

        assertEquals(FIELD_ERROR_MESSAGE, alertFieldStatus.getFieldMessage());
    }
}
