/**
 * component
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
        return samlContext.isSAMLEnabled() && super.processFilter(request);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        if (samlContext.isSAMLEnabled()) {
            alertLogger.debug("SAML Enabled commencing SAML entry point.");
            super.commence(request, response, e);
            return;
        }
        alertLogger.debug("AuthenticationException", e);
        if (e instanceof InsufficientAuthenticationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

}
