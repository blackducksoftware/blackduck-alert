/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.common.model.exception;

public class AlertRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -6181133427798683517L;

    public AlertRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlertRuntimeException(String message) {
        super(message);
    }

    public AlertRuntimeException(Throwable cause) {
        super(cause);
    }

}
