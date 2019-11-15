/**
 * alert-issuetracker
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.issuetracker.exception;

import java.util.Map;

public class IssueTrackerFieldException extends IssueTrackerException {
    private static final long serialVersionUID = -6064390279820606078L;
    private final Map<String, String> fieldErrors;

    public IssueTrackerFieldException(Map<String, String> fieldErrors) {
        super();
        this.fieldErrors = fieldErrors;
    }

    public IssueTrackerFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Map<String, String> fieldErrors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.fieldErrors = fieldErrors;
    }

    public IssueTrackerFieldException(String message, Throwable cause, Map<String, String> fieldErrors) {
        super(message, cause);
        this.fieldErrors = fieldErrors;
    }

    public IssueTrackerFieldException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public IssueTrackerFieldException(Throwable cause, Map<String, String> fieldErrors) {
        super(cause);
        this.fieldErrors = fieldErrors;
    }

    public static IssueTrackerFieldException singleFieldError(String fieldKey, String fieldError) {
        return new IssueTrackerFieldException(Map.of(fieldKey, fieldError));
    }

    public static IssueTrackerFieldException singleFieldError(String message, String fieldKey, String fieldError) {
        return new IssueTrackerFieldException(message, Map.of(fieldKey, fieldError));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
