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
package com.synopsys.integration.alert.web.api.home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLContext;

@Controller
public class HomeController {
    private static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    private final HttpSessionCsrfTokenRepository csrfTokenRespository;
    private final SAMLContext samlContext;

    @Autowired
    public HomeController(HttpSessionCsrfTokenRepository csrfTokenRepository, SAMLContext samlContext) {
        this.csrfTokenRespository = csrfTokenRepository;
        this.samlContext = samlContext;
    }

    @GetMapping(value = { "/", "/error", "/channels/**", "/providers/**", "/general/**" }, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    @GetMapping("/api/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkAuthentication(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRespository.loadToken(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication.getAuthorities().stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .anyMatch(authority -> authority.equals(ROLE_ANONYMOUS));
        boolean authorized = authentication.isAuthenticated() && !isAnonymous && csrfToken != null;

        if (!authorized) {
            request.getSession().invalidate();
            throw ResponseFactory.createUnauthorizedException();
        } else {
            response.addHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }
    }

    @ResponseBody
    @GetMapping("/api/verify/saml")
    public SAMLEnabledResponseModel checkSaml() {
        return new SAMLEnabledResponseModel(samlContext.isSAMLEnabled());
    }

}
