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
package com.synopsys.integration.alert.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.web.controller.handler.SystemHandler;
import com.synopsys.integration.alert.web.model.SystemSetupModel;

@RestController
public class SystemController extends BaseController {
    private final SystemHandler handler;

    @Autowired
    public SystemController(final SystemHandler handler) {
        this.handler = handler;
    }

    @GetMapping(value = "/system/messages/latest")
    public ResponseEntity<String> getLatestSystemMessages() {
        return handler.getLatestMessagesSinceStartup();
    }

    @GetMapping(value = "/system/messages")
    public ResponseEntity<String> getSystemMessages(@RequestParam(value = "startDate", required = false) final String startDate, @RequestParam(value = "endDate", required = false) final String endDate) {
        return handler.getSystemMessages(startDate, endDate);
    }

    @GetMapping(value = "/system/setup")
    public ResponseEntity<String> getSystemSetup() {
        return handler.getCurrentSetup();
    }

    @PostMapping(value = "/system/setup")
    public ResponseEntity<String> initialSystemSetup(@RequestBody final SystemSetupModel requiredSystemConfiguration) {
        return handler.saveRequiredInformation(requiredSystemConfiguration);
    }
}
