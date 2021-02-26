/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

@Deprecated(forRemoval = true)
public class AlertDatabaseConstraintException extends AlertException {
    private static final long serialVersionUID = 2057996253011099927L;

    public AlertDatabaseConstraintException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertDatabaseConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlertDatabaseConstraintException(String message) {
        super(message);
    }

    public AlertDatabaseConstraintException(Throwable cause) {
        super(cause);
    }

}
