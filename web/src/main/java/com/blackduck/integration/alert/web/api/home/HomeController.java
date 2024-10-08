/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.synopsys.integration.alert.common.rest.ResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
    private final HomeActions actions;

    @Autowired
    public HomeController(HomeActions actions) {
        this.actions = actions;
    }

    @GetMapping(value = { "/error" }, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index.html";
    }

    @ResponseBody
    @GetMapping(value = { "/api/verify" }, produces = MediaType.APPLICATION_JSON_VALUE)
    public VerifyAuthenticationResponseModel checkAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return ResponseFactory.createContentResponseFromAction(actions.verifyAuthentication(request, response));
    }

    @ResponseBody
    @GetMapping(value = { "/api/verify/saml" }, produces = MediaType.APPLICATION_JSON_VALUE)
    public SAMLEnabledResponseModel checkSaml() {
        return ResponseFactory.createContentResponseFromAction(actions.verifySaml());
    }
}
