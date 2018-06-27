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
package com.blackducksoftware.integration.hub.alert.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.web.controller.handler.LoginHandler;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;

@RestController
public class LoginController extends BaseController {
    private final LoginHandler loginHandler;

    @Autowired
    public LoginController(final LoginHandler loginDataHandler) {
        this.loginHandler = loginDataHandler;
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<String> logout(final HttpServletRequest request) {
        return loginHandler.userLogout(request);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(final HttpServletRequest request, final HttpServletResponse response, @RequestBody(required = false) final LoginRestModel loginRestModel) {
        return loginHandler.userLogin(request, response, loginRestModel);
    }
}
