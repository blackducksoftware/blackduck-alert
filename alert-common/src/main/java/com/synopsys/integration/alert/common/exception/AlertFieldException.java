/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class AlertFieldException extends AlertException {
    private static final long serialVersionUID = 7993564907680483145L;

    private final List<AlertFieldStatus> fieldErrors;

    public static AlertFieldException singleFieldError(String fieldKey, String fieldError) {
        return singleFieldError(AlertFieldStatus.error(fieldKey, fieldError));
    }

    public static AlertFieldException singleFieldError(AlertFieldStatus fieldError) {
        return new AlertFieldException(List.of(fieldError));
    }

    public static AlertFieldException singleFieldError(String message, String fieldKey, String fieldError) {
        return new AlertFieldException(message, List.of(AlertFieldStatus.error(fieldKey, fieldError)));
    }

    public AlertFieldException(List<AlertFieldStatus> fieldErrors) {
        super();
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<AlertFieldStatus> fieldErrors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(String message, Throwable cause, List<AlertFieldStatus> fieldErrors) {
        super(message, cause);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(String message, List<AlertFieldStatus> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public AlertFieldException(Throwable cause, List<AlertFieldStatus> fieldErrors) {
        super(cause);
        this.fieldErrors = fieldErrors;
    }

    public List<AlertFieldStatus> getFieldErrors() {
        return fieldErrors;
    }

    public String getFlattenedErrorMessages() {
        return fieldErrors
                   .stream()
                   .map(AlertFieldStatus::getFieldMessage)
                   .collect(Collectors.joining(", "));
    }

}
