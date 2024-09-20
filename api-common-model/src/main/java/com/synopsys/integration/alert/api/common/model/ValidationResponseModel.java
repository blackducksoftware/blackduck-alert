/*
 * api-common-model
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.common.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;

public class ValidationResponseModel extends AlertSerializableModel {
    public static final String VALIDATION_SUCCESS_MESSAGE = "The configuration is valid";
    public static final String VALIDATION_FAILURE_MESSAGE = "There were problems with the configuration";

    private String message;
    private Boolean hasErrors;
    private Map<String, AlertFieldStatus> errors;

    public static ValidationResponseModel success() {
        return success(VALIDATION_SUCCESS_MESSAGE);
    }

    public static ValidationResponseModel success(String message) {
        return new ValidationResponseModel(message, Map.of());
    }

    public static ValidationResponseModel generalError(String message) {
        return new ValidationResponseModel(message, true);
    }

    public static ValidationResponseModel fromStatusCollection(Collection<AlertFieldStatus> fieldStatuses) {
        return fromStatusCollection(VALIDATION_FAILURE_MESSAGE, fieldStatuses);
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
                }
            } else {
                fieldNameToStatus.put(fieldName, fieldStatus);
            }
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

    public ValidationResponseModel(String message, Map<String, AlertFieldStatus> statuses) {
        this.message = message;
        this.errors = statuses;
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
