/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.errors;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AlertFieldStatus extends AlertSerializableModel {
    private final String fieldName;
    private final FieldStatusSeverity severity;
    private final String fieldMessage;

    public static AlertFieldStatus error(String fieldName, String fieldErrorMessage) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.ERROR, fieldErrorMessage);
    }

    public static AlertFieldStatus warning(String fieldName, String fieldWarningMessage) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.WARNING, fieldWarningMessage);
    }

    public AlertFieldStatus(String fieldName, FieldStatusSeverity severity, String fieldMessage) {
        this.fieldName = fieldName;
        this.severity = severity;
        this.fieldMessage = fieldMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldStatusSeverity getSeverity() {
        return severity;
    }

    public String getFieldMessage() {
        return fieldMessage;
    }

}
