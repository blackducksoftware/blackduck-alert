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

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class AlertExceptionTest {
    private Set<Class<? extends Exception>> exceptionSet;

    @Before
    public void createExceptionSet() {
        exceptionSet = new LinkedHashSet<>();
        exceptionSet.add(AlertException.class);
        exceptionSet.add(AlertDatabaseConstraintException.class);
        exceptionSet.add(AlertLDAPConfigurationException.class);
        exceptionSet.add(AlertRuntimeException.class);
    }

    @Test
    public void testFullConstructor() throws Exception {
        for (final Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";
            final Throwable cause = new Throwable();
            final boolean enableSuppression = true;
            final boolean writableStackTrace = true;

            final Exception alertException = createFullConstructor(exceptionClass, message, cause, enableSuppression, writableStackTrace);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testMessageAndCauseConstructor() throws Exception {
        for (final Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";
            final Throwable cause = new Throwable();

            final Exception alertException = createMessageAndCauseConstructor(exceptionClass, message, cause);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testMessageOnlyConstructor() throws Exception {
        for (final Class<? extends Exception> exceptionClass : exceptionSet) {
            final String message = "Exception Message";

            final Exception alertException = createMessageConstructor(exceptionClass, message);
            assertNotNull(alertException);
        }
    }

    @Test
    public void testCauseOnlyConstructor() throws Exception {
        for (final Class<? extends Exception> exceptionClass : exceptionSet) {
            final Throwable cause = new Throwable();

            final Exception alertException = createCauseConstructor(exceptionClass, cause);
            assertNotNull(alertException);
        }
    }

    private <E> E createFullConstructor(final Class<E> exceptionClass, final String message, final Throwable throwable, final boolean enableSuppression, final boolean writableStackTrace) throws Exception {
        final Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class, Boolean.TYPE, Boolean.TYPE);
        return constructor.newInstance(message, throwable, enableSuppression, writableStackTrace);
    }

    private <E> E createMessageAndCauseConstructor(final Class<E> exceptionClass, final String message, final Throwable throwable) throws Exception {
        final Constructor<E> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(message, throwable);
    }

    private <E> E createMessageConstructor(final Class<E> exceptionClass, final String message) throws Exception {
        final Constructor<E> constructor = exceptionClass.getConstructor(String.class);
        return constructor.newInstance(message);
    }

    private <E> E createCauseConstructor(final Class<E> exceptionClass, final Throwable throwable) throws Exception {
        final Constructor<E> constructor = exceptionClass.getConstructor(Throwable.class);
        return constructor.newInstance(throwable);
    }
}
