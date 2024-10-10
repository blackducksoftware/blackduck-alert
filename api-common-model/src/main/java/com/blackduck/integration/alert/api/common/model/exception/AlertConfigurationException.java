/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.common.model.exception;

public class AlertConfigurationException extends AlertException {
    private static final long serialVersionUID = -1829641778306376398L;

    public AlertConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlertConfigurationException(String message) {
        super(message);
    }

    public AlertConfigurationException(Throwable cause) {
        super(cause);
    }

}
