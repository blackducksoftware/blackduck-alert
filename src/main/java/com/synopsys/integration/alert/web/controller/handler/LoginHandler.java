/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.controller.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.LoginActions;
import com.synopsys.integration.alert.web.model.LoginConfig;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

@Component
public class LoginHandler extends ControllerHandler {
    private final LoginActions loginActions;
    private final HttpSessionCsrfTokenRepository csrfTokenRepository;

    @Autowired
    public LoginHandler(final ContentConverter contentConverter, final LoginActions loginActions, final HttpSessionCsrfTokenRepository csrfTokenRepository) {
        super(contentConverter);
        this.loginActions = loginActions;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public ResponseEntity<String> userLogout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/");

        return new ResponseEntity<>("{\"message\":\"Success\"}", headers, HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<String> userLogin(final HttpServletRequest request, final HttpServletResponse response, final LoginConfig loginConfig) {
        return authenticateUser(request, response, loginConfig);
    }

    // TODO Create HandlerResponse to encapsulate the response  BaseController should convert HandlerResponse to ResponseEntity
    public ResponseEntity<String> authenticateUser(final HttpServletRequest request, final HttpServletResponse response, final LoginConfig loginConfig) {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        ResponseEntity<String> responseEntity;
        try {
            if (loginActions.authenticateUser(loginConfig)) {
                final CsrfToken token = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(token, request, response);
                response.setHeader(token.getHeaderName(), token.getToken());
                responseEntity = createResponse(HttpStatus.OK, "{\"message\":\"Success\"}");
            } else {
                responseEntity = createResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
            }
        } catch (final BadCredentialsException ex) {
            responseEntity = createResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            responseEntity = createResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return responseEntity;
    }
}
