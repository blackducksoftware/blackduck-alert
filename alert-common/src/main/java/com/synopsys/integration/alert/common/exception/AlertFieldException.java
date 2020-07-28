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
package com.synopsys.integration.alert.common.exception;

import java.util.HashMap;
import java.util.Map;

public class AlertFieldException extends AlertException {
    private static final long serialVersionUID = 7993564907680483145L;

    private final Map<String, AlertFieldStatus> fieldErrors;

    public static AlertFieldException singleFieldError(String fieldKey, String fieldError) {
        return new AlertFieldException(Map.of(fieldKey, fieldError));
    }

    public static AlertFieldException singleFieldError(String message, String fieldKey, String fieldError) {
        return new AlertFieldException(message, Map.of(fieldKey, fieldError));
    }

    public AlertFieldException(Map<String, String> fieldErrors) {
        super();
        this.fieldErrors = convertToAlertFieldStatus(fieldErrors);
    }

    public AlertFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Map<String, String> fieldErrors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.fieldErrors = convertToAlertFieldStatus(fieldErrors);
    }

    public AlertFieldException(String message, Throwable cause, Map<String, String> fieldErrors) {
        super(message, cause);
        this.fieldErrors = convertToAlertFieldStatus(fieldErrors);
    }

    public AlertFieldException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = convertToAlertFieldStatus(fieldErrors);
    }

    public AlertFieldException(Throwable cause, Map<String, String> fieldErrors) {
        super(cause);
        this.fieldErrors = convertToAlertFieldStatus(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        Map<String, String> fieldErrorsMap = new HashMap<>();
        for (Map.Entry<String, AlertFieldStatus> entry : fieldErrors.entrySet()) {
            fieldErrorsMap.put(entry.getKey(), entry.getValue().getFieldErrorMessage());
        }
        return fieldErrorsMap;
    }

    private Map<String, AlertFieldStatus> convertToAlertFieldStatus(Map<String, String> fieldErrors) {
        Map<String, AlertFieldStatus> fieldErrorsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldErrors.entrySet()) {
            fieldErrorsMap.put(entry.getKey(), createAlertFieldError(entry.getValue()));
        }
        return fieldErrorsMap;
    }

    private AlertFieldStatus createAlertFieldError(String errorMessage) {
        return new AlertFieldStatus(FieldErrorSeverity.ERROR, errorMessage);
    }

}
