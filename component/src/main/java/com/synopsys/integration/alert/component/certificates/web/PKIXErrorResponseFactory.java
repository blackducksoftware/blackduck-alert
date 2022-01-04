/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates.web;

import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.SSLValidationResponseModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptor;

@Component
public class PKIXErrorResponseFactory {
    private final Logger logger = LoggerFactory.getLogger(PKIXErrorResponseFactory.class);

    private static final String PKIX_HEADER = "There were issues with your Certificates.";
    private static final String PKIX_TITLE = "To resolve this issue, use the link below to properly install your certificates.";
    private static final String PKIX_MESSAGE = "Certificate page: ";

    private final Gson gson;

    @Autowired
    public PKIXErrorResponseFactory(Gson gson) {
        this.gson = gson;
    }

    public Optional<ValidationResponseModel> createSSLExceptionResponse(Exception e) {
        if (isPKIXError(e)) {
            logger.debug("Found an error regarding PKIX, creating a unique response...");
            logger.debug(e.getMessage(), e);
            String certificateLink = "/alert/components/" + CertificatesDescriptor.CERTIFICATES_URL;
            Map<String, Object> pkixErrorBody = Map.of("header", PKIX_HEADER, "title", PKIX_TITLE, "message", PKIX_MESSAGE,
                "componentLabel", CertificatesDescriptor.CERTIFICATES_LABEL, "componentLink", certificateLink);
            String pkixError = gson.toJson(pkixErrorBody);
            return Optional.of(new SSLValidationResponseModel(pkixError));
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
