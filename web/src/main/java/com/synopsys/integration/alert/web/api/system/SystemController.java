/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseController;

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
