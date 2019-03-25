/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.google.common.net.HttpHeaders;
import com.synopsys.integration.rest.RestConstants;

public class AlertSamlEntryPoint extends SAMLEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(AlertSamlEntryPoint.class);
    public static final String SAML_PROVIDER_NAME = "Synopsys - Alert";
    public static final String REQUEST_HEADER_FOR_UI_XHR = "XMLHttpRequest";

    private final WebSSOProfileOptions defaultWebSSOProfileOptions;

    public AlertSamlEntryPoint() {

        defaultWebSSOProfileOptions = new WebSSOProfileOptions();
        defaultWebSSOProfileOptions.setIncludeScoping(false);
        defaultWebSSOProfileOptions.setProviderName(SAML_PROVIDER_NAME);
        defaultWebSSOProfileOptions.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        defaultWebSSOProfileOptions.setForceAuthN(true);
        //TODO get property
        //        defaultWebSSOProfileOptions.setForceAuthN(samlContext.isLocalLogoutEnabled());
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException e) {
        if (true) {
            //TODO get property
            //        if (samlContext.isSamlSSOEnabled()) {

            // SAML is enabled, attempt to authorize via IDP.
            logger.debug("Trying to authenticate with Single Sign On.");
            try {
                response.addHeader("X-SAML-LOGIN", "true");
                if (REQUEST_HEADER_FOR_UI_XHR.equals(request.getHeader(HttpHeaders.X_REQUESTED_WITH))) {
                    response.setStatus(RestConstants.UNAUTHORIZED_401);
                }
                refreshForceAuthnenticationFlag();
                super.commence(request, response, e);
                return;
            } catch (final ServletException | RuntimeException | IOException ex) {
                throw new RuntimeException("Saml configuration is invalid.", ex);

            }
        } else {
            logger.debug("Single Sign On is disabled by administrator.");
        }
    }

    private void refreshForceAuthnenticationFlag() {
        //TODO get property
        // defaultWebSSOProfileOptions.setForceAuthN(samlContext.isLocalLogoutEnabled());
        defaultWebSSOProfileOptions.setForceAuthN(true);
        super.setDefaultProfileOptions(defaultWebSSOProfileOptions);
    }
}

