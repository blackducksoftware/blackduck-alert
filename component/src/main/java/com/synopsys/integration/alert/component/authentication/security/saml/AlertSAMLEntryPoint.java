/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;

public class AlertSAMLEntryPoint extends SAMLEntryPoint {
    // SonarCloud will fail the build if this variable is named 'logger' because there is a variable with the same name in a parent class.
    private final Logger alertLogger = LoggerFactory.getLogger(AlertSAMLEntryPoint.class);
    private final SAMLContext samlContext;

    public AlertSAMLEntryPoint(SAMLContext samlContext) {
        super();
        this.samlContext = samlContext;
    }

    @Override
    protected boolean processFilter(HttpServletRequest request) {
        return samlContext.isSAMLEnabledForRequest(request) && super.processFilter(request);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        if (samlContext.isSAMLEnabledForRequest(request)) {
            alertLogger.debug("SAML Enabled commencing SAML entry point.");
            super.commence(request, response, e);
        } else {
            alertLogger.debug("AuthenticationException", e);
            if (e instanceof InsufficientAuthenticationException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        }
    }

}
