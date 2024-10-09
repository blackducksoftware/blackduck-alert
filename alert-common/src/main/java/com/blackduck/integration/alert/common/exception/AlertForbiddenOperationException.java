package com.blackduck.integration.alert.common.exception;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public class AlertForbiddenOperationException extends AlertException {
    private static final long serialVersionUID = -3915058637337880806L;

    public AlertForbiddenOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlertForbiddenOperationException(String message) {
        super(message);
    }

    public AlertForbiddenOperationException(Throwable cause) {
        super(cause);
    }

    public AlertForbiddenOperationException() {
    }

}
