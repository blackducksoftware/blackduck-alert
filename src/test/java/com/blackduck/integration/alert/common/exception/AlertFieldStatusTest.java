/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.FieldStatusSeverity;

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
