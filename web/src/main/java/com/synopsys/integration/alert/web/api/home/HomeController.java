/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.api.home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.synopsys.integration.alert.common.rest.ResponseFactory;

@Controller
public class HomeController {
    private final HomeActions actions;

    @Autowired
    public HomeController(HomeActions actions) {
        this.actions = actions;
    }

    @GetMapping(value = { "/", "/error", "/channels/**", "/providers/**", "/general/**" }, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        /*
           This is using Thymeleaf templating therefore we cannot return just a string from an ActionResponse otherwise the templating will not be setup correctly.
           Alert will return a 404 on the main page because index.html will not be served up by this method.
        */
        return "index";
    }

    @GetMapping("/api/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ResponseFactory.createResponseFromAction(actions.verifyAuthentication(request, response));
    }

    @ResponseBody
    @GetMapping("/api/verify/saml")
    public SAMLEnabledResponseModel checkSaml() {
        return ResponseFactory.createContentResponseFromAction(actions.verifySaml());
    }

}
