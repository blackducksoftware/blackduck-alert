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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HttpSessionCsrfTokenRepository csrfTokenRespository;

    @Autowired
    public HomeController(final HttpSessionCsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRespository = csrfTokenRepository;
    }

    @GetMapping(value = { "/", "/error", "/channels/**", "/providers/**", "/general/**" }, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping(value = "/api/verify")
    public ResponseEntity<String> checkAuthentication(final HttpServletRequest request) {
        final HttpServletRequest httpRequest = request;
        final CsrfToken csrfToken = csrfTokenRespository.loadToken(request);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final boolean authorized = authentication.isAuthenticated() && csrfToken != null;

        if (!authorized) {
            httpRequest.getSession().invalidate();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            final String body = "{\"message\":\"Authenticated\"}";
            final HttpHeaders headers = new HttpHeaders();
            headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        }
    }
}
