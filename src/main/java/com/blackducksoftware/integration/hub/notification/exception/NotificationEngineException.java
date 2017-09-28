package com.blackducksoftware.integration.hub.notification.exception;

import com.blackducksoftware.integration.exception.IntegrationException;

public class NotificationEngineException extends IntegrationException {
    private static final long serialVersionUID = 7993564907680483145L;

    public NotificationEngineException() {
        super();
    }

    public NotificationEngineException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotificationEngineException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotificationEngineException(final String message) {
        super(message);
    }

    public NotificationEngineException(final Throwable cause) {
        super(cause);
    }
}
