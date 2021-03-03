/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

public class AlertRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -6181133427798683517L;

    public AlertRuntimeException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AlertRuntimeException(final String message) {
        super(message);
    }

    public AlertRuntimeException(final Throwable cause) {
        super(cause);
    }
}
