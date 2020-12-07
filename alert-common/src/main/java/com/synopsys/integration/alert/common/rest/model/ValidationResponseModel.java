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
package com.synopsys.integration.alert.common.rest.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;

public class ValidationResponseModel extends AlertSerializableModel {
    private String message;
    private Boolean hasErrors;
    private Map<String, AlertFieldStatus> errors;

    public static ValidationResponseModel success(String message) {
        return new ValidationResponseModel(message, Map.of());
    }

    public static ValidationResponseModel generalError(String message) {
        return new ValidationResponseModel(message, true);
    }

    public static ValidationResponseModel fromStatusCollection(String message, Collection<AlertFieldStatus> fieldStatuses) {
        Map<String, AlertFieldStatus> fieldNameToStatus = new HashMap<>();
        for (AlertFieldStatus fieldStatus : fieldStatuses) {
            String fieldName = fieldStatus.getFieldName();
            AlertFieldStatus existingStatus = fieldNameToStatus.get(fieldName);
            if (null != existingStatus) {
                if (existingStatus.getSeverity().equals(fieldStatus.getSeverity())) {
                    String combinedMessage = String.format("%s, %s", existingStatus.getFieldMessage(), fieldStatus.getFieldMessage());
                    fieldNameToStatus.put(fieldName, new AlertFieldStatus(fieldName, fieldStatus.getSeverity(), combinedMessage));
                } else if (FieldStatusSeverity.WARNING.equals(fieldStatus.getSeverity())) {
                    continue;
                }
            }
            fieldNameToStatus.put(fieldName, fieldStatus);
        }
        return new ValidationResponseModel(message, fieldNameToStatus);
    }

    public ValidationResponseModel() {
        // For serialization
    }

    protected ValidationResponseModel(String message, Boolean hasErrors) {
        this.message = message;
        this.errors = Map.of();
        this.hasErrors = hasErrors;
    }

    public ValidationResponseModel(String message, Map<String, AlertFieldStatus> errors) {
        this.message = message;
        this.errors = errors;
        this.hasErrors = !errors.isEmpty();
    }

    public String getMessage() {
        return message;
    }

    public Map<String, AlertFieldStatus> getErrors() {
        return errors;
    }

    @JsonProperty("hasErrors")
    public boolean hasErrors() {
        return hasErrors;
    }

}
