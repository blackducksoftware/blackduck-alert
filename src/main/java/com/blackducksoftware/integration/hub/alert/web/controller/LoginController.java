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
package com.blackducksoftware.integration.hub.alert.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.actions.LoginActions;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

@RestController
public class LoginController {
    private final LoginActions loginActions;

    @Autowired
    public LoginController(final LoginActions loginActions) {
        this.loginActions = loginActions;
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<String> logout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request, @RequestBody(required = false) final LoginRestModel loginRestModel) {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            // TODO figure out timeout
            session.setMaxInactiveInterval(300);
        }
        try {
            if (loginActions.authenticateUser(loginRestModel, logger)) {
                return new ResponseEntity<>("{\"message\":\"Success\"}", HttpStatus.ACCEPTED);
            }
            return createResponse(HttpStatus.UNAUTHORIZED, "User not administrator");
        } catch (final IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.valueOf(e.getHttpStatusCode()), e.getHttpStatusMessage() + " : " + e.getMessage());
        } catch (final AlertFieldException e) {
            final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(0L, e.getMessage());
            responseBodyBuilder.putErrors(e.getFieldErrors());
            final String responseBody = responseBodyBuilder.build();
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<String> createResponse(final HttpStatus status, final String message) {
        final String responseBody = new ResponseBodyBuilder(-1L, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

}
