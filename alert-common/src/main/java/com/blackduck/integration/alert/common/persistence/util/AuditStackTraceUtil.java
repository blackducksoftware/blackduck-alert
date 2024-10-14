/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public final class AuditStackTraceUtil {

    public static final int STACK_TRACE_CHAR_LIMIT = 10000;
    
    public static String createStackTraceString(Throwable throwable) {
        String[] rootCause = ExceptionUtils.getRootCauseStackTrace(throwable);
        String exceptionStackTrace = "";
        for (String line : rootCause) {
            if (exceptionStackTrace.length() + line.length() < AuditStackTraceUtil.STACK_TRACE_CHAR_LIMIT) {
                exceptionStackTrace = String.format("%s%s%s", exceptionStackTrace, line, System.lineSeparator());
            } else {
                break;
            }
        }
        return exceptionStackTrace;
    }

    private AuditStackTraceUtil() {
        // cannot construct
    }
}
