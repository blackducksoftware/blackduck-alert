/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.http;

import org.apache.http.HttpStatus;

import com.blackduck.integration.exception.IntegrationException;

public class HttpServiceException extends IntegrationException {
    private final int httpErrorCode;

    public static HttpServiceException internalServerError(String message) {
        return new HttpServiceException(message, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public static HttpServiceException internalServerError(Throwable cause) {
        return new HttpServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, cause);
    }

    public static HttpServiceException forbiddenError(String message) {
        return new HttpServiceException(message, HttpStatus.SC_FORBIDDEN);
    }

    public static HttpServiceException forbiddenError(String message, Throwable cause) {
        return new HttpServiceException(message, HttpStatus.SC_FORBIDDEN, cause);
    }

    public static HttpServiceException notFoundError(String message) {
        return new HttpServiceException(message, HttpStatus.SC_NOT_FOUND);
    }

    public HttpServiceException(int httpErrorCode) {
        this.httpErrorCode = httpErrorCode;
    }

    public HttpServiceException(String message, int httpErrorCode) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }

    public HttpServiceException(String message, int httpErrorCode, Throwable cause) {
        super(message, cause);
        this.httpErrorCode = httpErrorCode;
    }

    public HttpServiceException(int httpErrorCode, Throwable cause) {
        super(cause);
        this.httpErrorCode = httpErrorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

}
