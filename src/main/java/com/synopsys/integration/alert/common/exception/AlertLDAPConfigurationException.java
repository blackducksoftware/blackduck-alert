package com.synopsys.integration.alert.common.exception;

public class AlertLDAPConfigurationException extends AlertException {
    private static final long serialVersionUID = -1829641778306376398L;

    public AlertLDAPConfigurationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlertLDAPConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AlertLDAPConfigurationException(final String message) {
        super(message);
    }

    public AlertLDAPConfigurationException(final Throwable cause) {
        super(cause);
    }
}
