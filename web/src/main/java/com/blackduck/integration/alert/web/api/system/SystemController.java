/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseController;

@RestController
public class SystemController extends BaseController {
    private final SystemActions systemActions;

    @Autowired
    public SystemController(SystemActions systemActions) {
        this.systemActions = systemActions;
    }

    @GetMapping(value = "/system/messages/latest")
    public MultiSystemMessageModel getLatestSystemMessages() {
        return ResponseFactory.createContentResponseFromAction(systemActions.getSystemMessagesSinceStartup());

    }

    @GetMapping(value = "/system/messages")
    public MultiSystemMessageModel getSystemMessages(@RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate) {
        return ResponseFactory.createContentResponseFromAction(systemActions.getSystemMessages(startDate, endDate));
    }
}
