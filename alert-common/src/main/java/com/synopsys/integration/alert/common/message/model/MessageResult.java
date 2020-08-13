/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.message.model;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageResult extends AlertSerializableModel {
    private final String statusMessage;
    private final List<AlertFieldStatus> fieldStatuses;

    public static MessageResult singleFieldError(String fieldName, String errorMessage) {
        AlertFieldStatus fieldError = AlertFieldStatus.error(fieldName, errorMessage);
        return new MessageResult("Field error", List.of(fieldError));
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
        return fieldStatuses
                   .stream()
                   .filter(status -> status.getSeverity().equals(severity))
                   .collect(Collectors.toList());
    }

    public boolean hasFieldStatusBySeverity(FieldStatusSeverity severity) {
        return fieldStatuses
                   .stream()
                   .map(AlertFieldStatus::getSeverity)
                   .anyMatch(severity::equals);
    }

}
