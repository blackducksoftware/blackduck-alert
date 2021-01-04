/**
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
