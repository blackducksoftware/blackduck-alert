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
package com.synopsys.integration.alert.web.security.authentication.saml;

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
    private final Logger logger = LoggerFactory.getLogger(AlertSAMLEntryPoint.class);

    private final SAMLContext samlContext;

    public AlertSAMLEntryPoint(final SAMLContext samlContext) {
        super();
        this.samlContext = samlContext;
    }

    @Override
    protected boolean processFilter(final HttpServletRequest request) {
        return samlContext.isSAMLEnabled() && super.processFilter(request);
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException e) throws IOException, ServletException {
        if (samlContext.isSAMLEnabled()) {
            super.commence(request, response, e);
            return;
        }

        if (e instanceof InsufficientAuthenticationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
