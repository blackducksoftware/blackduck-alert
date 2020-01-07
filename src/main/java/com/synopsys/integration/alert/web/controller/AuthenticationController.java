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
package com.synopsys.integration.alert.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.component.settings.PasswordResetService;
import com.synopsys.integration.alert.web.actions.LoginActions;
import com.synopsys.integration.alert.web.model.LoginConfig;

@RestController
public class AuthenticationController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LoginActions loginActions;
    private final PasswordResetService passwordResetService;
    private final ResponseFactory responseFactory;
    private final CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public AuthenticationController(final LoginActions loginActions, final PasswordResetService passwordResetService, final ResponseFactory responseFactory, final CsrfTokenRepository csrfTokenRepository) {
        this.loginActions = loginActions;
        this.passwordResetService = passwordResetService;
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

        return responseFactory.createResponse(HttpStatus.NO_CONTENT, headers, "{\"message\":\"Success\"}");
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request, final HttpServletResponse response, @RequestBody(required = false) final LoginConfig loginConfig) {
        try {
            if (loginActions.authenticateUser(loginConfig)) {
                final CsrfToken token = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(token, request, response);
                response.setHeader(token.getHeaderName(), token.getToken());
                return responseFactory.createMessageResponse(HttpStatus.OK, "Success");
            } else {
                return responseFactory.createMessageResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
            }
        } catch (final BadCredentialsException ex) {
            return responseFactory.createMessageResponse(HttpStatus.UNAUTHORIZED, "User not authorized");
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping(value = "/resetPassword")
    public ResponseEntity<String> resetPassword() {
        return responseFactory.createBadRequestResponse(ResponseFactory.EMPTY_ID, "Password Reset Error: A username must be specified");
    }

    @PostMapping(value = "/resetPassword/{username}")
    public ResponseEntity<String> resetPassword(@PathVariable final String username) {
        final String errorPrefix = "Password Reset Error: ";
        try {
            passwordResetService.resetPassword(username);
            return responseFactory.createOkResponse(ResponseFactory.EMPTY_ID, "Password reset email sent");
        } catch (final AlertDatabaseConstraintException databaseException) {
            return responseFactory.createFieldErrorResponse(ResponseFactory.EMPTY_ID, errorPrefix + "Invalid username", Map.of("username", databaseException.getMessage()));
        } catch (final AlertException e) {
            return responseFactory.createInternalServerErrorResponse(ResponseFactory.EMPTY_ID, errorPrefix + e.getMessage());
        }
    }
}
