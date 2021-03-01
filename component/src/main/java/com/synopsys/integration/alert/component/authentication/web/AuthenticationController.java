/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.web;

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
import com.synopsys.integration.alert.common.rest.api.BaseController;

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
