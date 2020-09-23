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
package com.synopsys.integration.alert.web.api.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
public class AuthenticationController extends BaseController {
    private final AuthenticationActions authenticationActions;
    private final PasswordResetService passwordResetService;
    private final CsrfTokenRepository csrfTokenRepository;

    @Autowired
    public AuthenticationController(AuthenticationActions authenticationActions, PasswordResetService passwordResetService, CsrfTokenRepository csrfTokenRepository) {
        this.authenticationActions = authenticationActions;
        this.passwordResetService = passwordResetService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping(value = "/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseFactory.createResponseFromAction(authenticationActions.logout(request, response));
    }

    @PostMapping(value = "/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) LoginConfig loginConfig) {
        ResponseFactory.createResponseFromAction(authenticationActions.authenticateUser(request, response, loginConfig));
    }

    @PostMapping(value = "/resetPassword/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable(required = false) String username) {
        ResponseFactory.createResponseFromAction(authenticationActions.resetPassword(username));
    }
}
