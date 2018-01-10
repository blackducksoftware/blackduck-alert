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
package com.blackducksoftware.integration.hub.alert.exception;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AlertExceptionTest {

    @Test
    public void testEmptyConstructor() {
        final AlertException alertException = new AlertException();
        assertNotNull(alertException);
    }

    @Test
    public void testFullConstructor() {
        final String message = "Exception Message";
        final Throwable cause = new Throwable();
        final boolean enableSuppression = true;
        final boolean writableStackTrace = true;

        final AlertException alertException = new AlertException(message, cause, enableSuppression, writableStackTrace);
        assertNotNull(alertException);
    }

    @Test
    public void testMessageAndCauseConstructor() {
        final String message = "Exception Message";
        final Throwable cause = new Throwable();

        final AlertException alertException = new AlertException(message, cause);
        assertNotNull(alertException);
    }

    @Test
    public void testMessageOnlyConstructor() {
        final String message = "Exception Message";

        final AlertException alertException = new AlertException(message);
        assertNotNull(alertException);
    }

    @Test
    public void testCauseOnlyConstructor() {
        final Throwable cause = new Throwable();

        final AlertException alertException = new AlertException(cause);
        assertNotNull(alertException);
    }

}
