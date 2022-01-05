/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.message.model;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.exception.AlertFieldException;

public class MessageResult extends AlertSerializableModel {
    private static final String STATUS_MESSAGE_SUCCESS = "Success";
    private static final MessageResult MESSAGE_RESULT_SUCCESS = new MessageResult(STATUS_MESSAGE_SUCCESS);

    private final String statusMessage;
    private final List<AlertFieldStatus> fieldStatuses;

    public static List<AlertFieldStatus> getFieldStatusesBySeverity(List<AlertFieldStatus> fieldStatuses, FieldStatusSeverity severity) {
        return fieldStatuses
                   .stream()
                   .filter(status -> status.getSeverity().equals(severity))
                   .collect(Collectors.toList());
    }

    public static boolean hasFieldStatusBySeverity(List<AlertFieldStatus> fieldStatuses, FieldStatusSeverity severity) {
        return fieldStatuses
                   .stream()
                   .map(AlertFieldStatus::getSeverity)
                   .anyMatch(severity::equals);
    }

    public static MessageResult success() {
        return MESSAGE_RESULT_SUCCESS;
    }

    public MessageResult(String statusMessage) {
        this.statusMessage = statusMessage;
        fieldStatuses = List.of();
    }

    public MessageResult(String statusMessage, List<AlertFieldStatus> fieldStatuses) {
        this.statusMessage = statusMessage;
        this.fieldStatuses = fieldStatuses;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public List<AlertFieldStatus> getFieldStatuses() {
        return fieldStatuses;
    }

    public void throwExceptionForFieldStatues() throws AlertFieldException {
        if (!fieldStatuses.isEmpty()) {
            throw new AlertFieldException(statusMessage, fieldStatuses);
        }
    }

    public boolean hasErrors() {
        return hasFieldStatusBySeverity(FieldStatusSeverity.ERROR);
    }

    public List<AlertFieldStatus> fieldErrors() {
        return getFieldStatusesBySeverity(FieldStatusSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return hasFieldStatusBySeverity(FieldStatusSeverity.WARNING);
    }

    public List<AlertFieldStatus> fieldWarnings() {
        return getFieldStatusesBySeverity(FieldStatusSeverity.WARNING);
    }

    public List<AlertFieldStatus> getFieldStatusesBySeverity(FieldStatusSeverity severity) {
        return getFieldStatusesBySeverity(fieldStatuses, severity);
    }

    public boolean hasFieldStatusBySeverity(FieldStatusSeverity severity) {
        return hasFieldStatusBySeverity(fieldStatuses, severity);
    }

}
