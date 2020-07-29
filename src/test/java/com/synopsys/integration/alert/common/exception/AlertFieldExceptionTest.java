/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.common.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AlertFieldExceptionTest {

    @Test
    public void testBaseConstructor() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();

        AlertFieldException alertFieldException = new AlertFieldException(fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testFullConstructor() {
        final String message = "Exception Message";
        Throwable cause = new Throwable();
        final boolean enableSuppression = true;
        final boolean writableStackTrace = true;
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, cause, enableSuppression, writableStackTrace, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testMessageAndCauseConstructor() {
        final String message = "Exception Message";
        Throwable cause = new Throwable();
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, cause, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testMessageOnlyConstructor() {
        final String message = "Exception Message";
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();

        AlertFieldException alertFieldException = new AlertFieldException(message, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void testCauseOnlyConstructor() {
        Throwable cause = new Throwable();
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();

        AlertFieldException alertFieldException = new AlertFieldException(cause, fieldErrors);
        assertNotNull(alertFieldException);
    }

    @Test
    public void getFieldErrorsTest() {
        final String key1 = "key1";
        final String key2 = "key2";

        AlertFieldStatus value1 = new AlertFieldStatus(FieldErrorSeverity.WARNING, "value1");
        AlertFieldStatus value2 = new AlertFieldStatus(FieldErrorSeverity.ERROR, "value2");

        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        fieldErrors.put(key1, value1);
        fieldErrors.put(key2, value2);

        AlertFieldException alertFieldException = new AlertFieldException(fieldErrors);

        assertEquals(2, alertFieldException.getFieldErrors().size());
        assertEquals(value1, alertFieldException.getFieldErrors().get(key1));
        assertEquals(value2, alertFieldException.getFieldErrors().get(key2));
    }

}
