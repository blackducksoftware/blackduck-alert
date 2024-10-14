/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class ActionResponse<T> {
    public static final String FORBIDDEN_MESSAGE = "User not authorized to perform the request";

    public static <T> ActionResponse<T> createForbiddenResponse() {
        return new ActionResponse<>(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
    }

    private final HttpStatus httpStatus;
    private final String message;
    private final T content;

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
