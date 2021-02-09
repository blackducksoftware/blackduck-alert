/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action;

import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.http.HttpStatus;

public class ActionResponse<T> {
    public static final String FORBIDDEN_MESSAGE = "User not authorized to perform the request";

    public static final <T> ActionResponse<T> createForbiddenResponse() {
        return new ActionResponse<>(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
    }

    private HttpStatus httpStatus;
    private String message;
    private T content;

    public ActionResponse(HttpStatus httpStatus) {
        this(httpStatus, null, null);
    }

    public ActionResponse(HttpStatus httpStatus, @Nullable String message) {
        this(httpStatus, message, null);
    }

    public ActionResponse(HttpStatus httpStatus, @Nullable T content) {
        this(httpStatus, null, content);
    }

    public ActionResponse(HttpStatus httpStatus, @Nullable String message, @Nullable T content) {
        this.httpStatus = httpStatus;
        this.content = content;
        this.message = message;
    }

    public boolean isSuccessful() {
        return httpStatus.is2xxSuccessful();
    }

    public boolean isError() {
        return httpStatus.isError();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<T> getContent() {
        return Optional.ofNullable(content);
    }

    public boolean hasContent() {
        return getContent().isPresent();
    }

    public boolean hasMessage() { return getMessage().isPresent(); }
}
