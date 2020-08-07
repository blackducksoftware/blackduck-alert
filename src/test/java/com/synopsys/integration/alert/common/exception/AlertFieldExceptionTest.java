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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

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
