/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseController;

@RestController
public class AuthenticationController extends BaseController {
    private final AuthenticationActions authenticationActions;

    @Autowired
    public AuthenticationController(AuthenticationActions authenticationActions) {
        this.authenticationActions = authenticationActions;
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
}
