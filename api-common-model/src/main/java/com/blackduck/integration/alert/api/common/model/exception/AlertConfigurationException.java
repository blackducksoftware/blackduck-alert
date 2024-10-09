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
