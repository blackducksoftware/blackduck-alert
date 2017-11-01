/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.exception;

import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;

public class AlertFieldException extends IntegrationException {
    private static final long serialVersionUID = 7993564907680483145L;

    private final Map<String, String> fieldErrors;

    public AlertFieldException(final Map<String, String> fieldErrors) {
        super();
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace, final Map<String, String> fieldErrors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(final String message, final Throwable cause, final Map<String, String> fieldErrors) {
        super(message, cause);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(final String message, final Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(final Throwable cause, final Map<String, String> fieldErrors) {
        super(cause);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

}
