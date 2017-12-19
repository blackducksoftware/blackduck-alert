/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

public abstract class ControllerHandler {
    private final ObjectTransformer objectTransformer;

    public ControllerHandler(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    public ResponseEntity<String> doNotAllowHttpMethod() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String id, final String message) {
        return createResponse(status, objectTransformer.stringToLong(id), message);
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String message) {
        return createResponse(status, -1L, message);
    }

}
