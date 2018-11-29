package com.synopsys.integration.alert.common.exception;

public class AlertDatabaseConstraintException extends AlertException {
    private static final long serialVersionUID = 2057996253011099927L;

    public AlertDatabaseConstraintException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertDatabaseConstraintException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AlertDatabaseConstraintException(final String message) {
        super(message);
    }

    public AlertDatabaseConstraintException(final Throwable cause) {
        super(cause);
    }
}
