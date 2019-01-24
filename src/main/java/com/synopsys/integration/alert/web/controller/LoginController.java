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
package com.synopsys.integration.alert.web.controller;

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
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.web.actions.LoginActions;
import com.synopsys.integration.alert.web.model.LoginConfig;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

@RestController
public class LoginController extends BaseController {
    private final LoginActions loginActions;
    private ResponseFactory responseFactory;
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public LoginController(final LoginActions loginActions, final ResponseFactory responseFactory, final CsrfTokenRepository csrfTokenRepository) {
        this.loginActions = loginActions;
        this.responseFactory = responseFactory;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<String> logout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/");

        return new ResponseEntity<>("{\"message\":\"Success\"}", headers, HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request, final HttpServletResponse response, @RequestBody(required = false) final LoginConfig loginConfig) {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        try {
            if (loginActions.authenticateUser(loginConfig)) {
                final CsrfToken token = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(token, request, response);
                response.setHeader(token.getHeaderName(), token.getToken());
                return responseFactory.createResponse(HttpStatus.OK, "{\"message\":\"Success\"}");
            } else {
                return responseFactory.createResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
            }
        } catch (final BadCredentialsException ex) {
            return responseFactory.createResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            return responseFactory.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
