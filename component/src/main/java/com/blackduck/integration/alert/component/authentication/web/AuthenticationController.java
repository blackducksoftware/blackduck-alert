/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public AuthenticationResponseModel login(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) LoginConfig loginConfig) {
        return ResponseFactory.createContentResponseFromAction(authenticationActions.authenticateUser(request, response, loginConfig));
    }
}
