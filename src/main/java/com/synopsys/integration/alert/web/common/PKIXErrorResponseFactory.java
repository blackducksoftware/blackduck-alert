/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.common;

import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.ResponseBodyBuilder;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptor;

@Component
public class PKIXErrorResponseFactory {
    private final Logger logger = LoggerFactory.getLogger(PKIXErrorResponseFactory.class);

    private static final String PKIX_HEADER = "There were issues with your Certificates.";
    private static final String PKIX_TITLE = "To resolve this issue, use the link below to properly install your certificates.";
    private static final String PKIX_MESSAGE = "Certificate page: ";

    private final Gson gson;
    private final ResponseFactory responseFactory;

    @Autowired
    public PKIXErrorResponseFactory(Gson gson, ResponseFactory responseFactory) {
        this.gson = gson;
        this.responseFactory = responseFactory;
    }

    public Optional<ResponseEntity<String>> createSSLExceptionResponse(String id, Exception e) {
        if (isPKIXError(e)) {
            logger.debug("Found an error regarding PKIX, creating a unique response...");
            logger.debug(e.getMessage(), e);
            String certificateLink = "/alert/components/" + CertificatesDescriptor.CERTIFICATES_URL;
            Map<String, Object> pkixErrorBody = Map.of("header", PKIX_HEADER, "title", PKIX_TITLE, "message", PKIX_MESSAGE,
                "componentLabel", CertificatesDescriptor.CERTIFICATES_LABEL, "componentLink", certificateLink);
            String pkixError = gson.toJson(pkixErrorBody);
            ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(id, pkixError);
            responseBodyBuilder.put("isDetailed", true);
            ResponseEntity<String> badRequestResponse = responseFactory.createBadRequestResponse(id, responseBodyBuilder.build());
            return Optional.of(badRequestResponse);
        }

        return Optional.empty();
    }

    private boolean isPKIXError(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof SSLHandshakeException) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

}
