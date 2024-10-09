/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.about;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseController;

@RestController
public class AboutController extends BaseController {
    private final AboutActions aboutActions;

    @Autowired
    public AboutController(AboutActions aboutActions) {
        this.aboutActions = aboutActions;
    }

    @GetMapping(value = "/about")
    public AboutModel getAbout() {
        return ResponseFactory.createContentResponseFromAction(aboutActions.getAboutModel());
    }

}
