/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.common.model.exception;

import com.blackduck.integration.exception.IntegrationException;

public class AlertException extends IntegrationException {
    private static final long serialVersionUID = 7993564907680483145L;

    public AlertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlertException(String message) {
        super(message);
    }

    public AlertException(Throwable cause) {
        super(cause);
    }

    public AlertException() {
    }

}
