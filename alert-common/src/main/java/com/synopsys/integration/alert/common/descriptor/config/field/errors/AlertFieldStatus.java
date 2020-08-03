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
package com.synopsys.integration.alert.common.descriptor.config.field.errors;

public class AlertFieldStatus {
    private final FieldErrorSeverity severity;
    private final String fieldMessage;

    public static AlertFieldStatus error(String fieldErrorMessage) {
        return new AlertFieldStatus(FieldErrorSeverity.ERROR, fieldErrorMessage);
    }

    public static AlertFieldStatus warning(String fieldErrorMessage) {
        return new AlertFieldStatus(FieldErrorSeverity.WARNING, fieldErrorMessage);
    }

    private AlertFieldStatus(FieldErrorSeverity severity, String fieldMessage) {
        this.severity = severity;
        this.fieldMessage = fieldMessage;
    }

    public FieldErrorSeverity getSeverity() {
        return severity;
    }

    public String getFieldMessage() {
        return fieldMessage;

    }
}
