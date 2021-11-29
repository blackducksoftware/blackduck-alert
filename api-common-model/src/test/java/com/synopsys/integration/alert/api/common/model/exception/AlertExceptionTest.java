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
package com.synopsys.integration.alert.api.common.model.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlertExceptionTest {
    private Set<Class<? extends Exception>> exceptionSet;

    @BeforeEach
    public void createExceptionSet() {
        exceptionSet = new LinkedHashSet<>();
        exceptionSet.add(AlertException.class);
        exceptionSet.add(AlertConfigurationException.class);
        exceptionSet.add(AlertRuntimeException.class);
    }

    @Test
    public void emptyConstructorTest() {
        try {
            throw new AlertException();
        } catch (Exception e) {
            // Pass
        }
    }

    @Test
    public void testFullConstructor() throws Exception {
        for (Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";
            Throwable cause = new Throwable();
            final boolean enableSuppression = true;
            final boolean writableStackTrace = true;

            Exception alertException = createFullConstructor(exceptionClass, message, cause, enableSuppression, writableStackTrace);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testMessageAndCauseConstructor() throws Exception {
        for (Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";
            Throwable cause = new Throwable();

            Exception alertException = createMessageAndCauseConstructor(exceptionClass, message, cause);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testMessageOnlyConstructor() throws Exception {
        for (Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";

            Exception alertException = createMessageConstructor(exceptionClass, message);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testCauseOnlyConstructor() throws Exception {
        for (Class<? extends Exception> exceptionClass : exceptionSet) {
            Throwable cause = new Throwable();

            Exception alertException = createCauseConstructor(exceptionClass, cause);
            assertNotNull(alertException);
        }
    }

    private <E> E createFullConstructor(Class<E> exceptionClass, String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) throws Exception {
        Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class, Boolean.TYPE, Boolean.TYPE);
        return constructor.newInstance(message, throwable, enableSuppression, writableStackTrace);
    }

    private <E> E createMessageAndCauseConstructor(Class<E> exceptionClass, String message, Throwable throwable) throws Exception {
        Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(message, throwable);
    }

    private <E> E createMessageConstructor(Class<E> exceptionClass, String message) throws Exception {
        Constructor<E> constructor = exceptionClass.getConstructor(String.class);
        return constructor.newInstance(message);
    }

    private <E> E createCauseConstructor(Class<E> exceptionClass, Throwable throwable) throws Exception {
        Constructor<E> constructor = exceptionClass.getConstructor(Throwable.class);
        return constructor.newInstance(throwable);
    }

}
