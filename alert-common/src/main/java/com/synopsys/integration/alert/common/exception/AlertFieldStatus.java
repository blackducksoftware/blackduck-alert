package com.synopsys.integration.alert.common.exception;

public class AlertFieldStatus {
    private final FieldErrorSeverity severity;
    private final String fieldErrorMessage;

    public AlertFieldStatus(FieldErrorSeverity severity, String fieldErrorMessage) {
        this.severity = severity;
        this.fieldErrorMessage = fieldErrorMessage;
    }

    public FieldErrorSeverity getSeverity() {
        return severity;
    }

    public String getFieldErrorMessage() {
        return fieldErrorMessage;

    }
}
