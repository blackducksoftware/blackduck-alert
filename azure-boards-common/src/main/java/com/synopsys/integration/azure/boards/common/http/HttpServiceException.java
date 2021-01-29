/*
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.http;

import org.apache.http.HttpStatus;

import com.synopsys.integration.exception.IntegrationException;

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
