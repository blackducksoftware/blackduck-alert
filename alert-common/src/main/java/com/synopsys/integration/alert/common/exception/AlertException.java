/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

import com.synopsys.integration.exception.IntegrationException;

public class AlertException extends IntegrationException {
    private static final long serialVersionUID = 7993564907680483145L;

    public AlertException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AlertException(final String message) {
        super(message);
    }

    public AlertException(final Throwable cause) {
        super(cause);
    }

    public AlertException() {
    }
}
