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
