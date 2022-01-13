/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.errors;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AlertFieldStatus extends AlertSerializableModel {
    private final String fieldName;
    private final FieldStatusSeverity severity;
    private final String fieldMessage;
    private final String messageKey;

    public static AlertFieldStatus error(String fieldName, String fieldErrorMessage) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.ERROR, fieldErrorMessage, null);
    }

    public static AlertFieldStatus warning(String fieldName, String fieldWarningMessage) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.WARNING, fieldWarningMessage, null);
    }

    public static AlertFieldStatus errorWithMessageKey(String fieldName, String messageKey) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.ERROR, StringUtils.EMPTY, messageKey);
    }

    public static AlertFieldStatus warningWithMessageKey(String fieldName, String messageKey) {
        return new AlertFieldStatus(fieldName, FieldStatusSeverity.WARNING, StringUtils.EMPTY, messageKey);
    }

    public AlertFieldStatus(String fieldName, FieldStatusSeverity severity, String fieldMessage, @Nullable String messageKey) {
        this.fieldName = fieldName;
        this.severity = severity;
        this.fieldMessage = fieldMessage;
        this.messageKey = messageKey;
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

    public Optional<String> getMessageKey() {
        return Optional.ofNullable(messageKey);
    }

}
