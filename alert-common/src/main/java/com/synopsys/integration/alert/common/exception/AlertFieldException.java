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

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class AlertFieldException extends AlertException {
    private static final long serialVersionUID = 7993564907680483145L;

    private final List<AlertFieldStatus> fieldErrors;

    public static AlertFieldException singleFieldError(String fieldKey, String fieldError) {
        return new AlertFieldException(List.of(AlertFieldStatus.error(fieldKey, fieldError)));
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
