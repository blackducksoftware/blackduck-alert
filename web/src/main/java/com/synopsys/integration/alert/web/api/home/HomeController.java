/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

    @GetMapping(value = { "/error" }, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index.html";
    }

    @GetMapping("/api/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ResponseFactory.createResponseFromAction(actions.verifyAuthentication(request, response));
    }

    @ResponseBody
    @GetMapping("/api/verify/saml")
    public SAMLEnabledResponseModel checkSaml(HttpServletRequest request) {
        return ResponseFactory.createContentResponseFromAction(actions.verifySaml(request));
    }

}
