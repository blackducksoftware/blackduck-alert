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
import java.util.Map;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.util.DataStructureUtils;

public class ValidationResponseModel extends AlertSerializableModel {
    private String message;
    private boolean hasErrors;
    private Map<String, AlertFieldStatus> errors;

    public static ValidationResponseModel success(String message) {
        return new ValidationResponseModel(message, Map.of());
    }

    public static ValidationResponseModel genericError(String message) {
        ValidationResponseModel invalid = new ValidationResponseModel(message, Map.of());
        invalid.hasErrors = true;
        return invalid;
    }

    public static ValidationResponseModel fromStatusCollection(String message, Collection<AlertFieldStatus> fieldStatuses) {
        Map<String, AlertFieldStatus> fieldNameToStatus = DataStructureUtils.mapToValues(fieldStatuses, AlertFieldStatus::getFieldName);
        return new ValidationResponseModel(message, fieldNameToStatus);
    }

    public ValidationResponseModel() {
        // For serialization
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

    public boolean hasErrors() {
        return hasErrors;
    }

}
