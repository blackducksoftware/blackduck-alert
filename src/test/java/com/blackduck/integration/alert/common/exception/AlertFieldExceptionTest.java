/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;

public class AlertFieldExceptionTest {

    @Test
    public void testBaseConstructor() {
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        AlertFieldException alertFieldException = new AlertFieldException(fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testFullConstructor() {
        final String message = "Exception Message";
        Throwable cause = new Throwable();
        final boolean enableSuppression = true;
        final boolean writableStackTrace = true;
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, cause, enableSuppression, writableStackTrace, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testMessageAndCauseConstructor() {
        final String message = "Exception Message";
        Throwable cause = new Throwable();
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, cause, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testMessageOnlyConstructor() {
        final String message = "Exception Message";
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testCauseOnlyConstructor() {
        Throwable cause = new Throwable();
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        AlertFieldException alertFieldException = new AlertFieldException(cause, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void getFieldErrorsTest() {
        final String key1 = "key1";
        final String key2 = "key2";

        AlertFieldStatus value1 = AlertFieldStatus.warning(key1, "value1");
        AlertFieldStatus value2 = AlertFieldStatus.error(key2, "value2");

        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        fieldErrors.add(value1);
        fieldErrors.add(value2);

        AlertFieldException alertFieldException = new AlertFieldException(fieldErrors);

        assertEquals(2, alertFieldException.getFieldErrors().size());
        assertTrue(alertFieldException.getFieldErrors().contains(value1));
        assertTrue(alertFieldException.getFieldErrors().contains(value2));
    }

}
