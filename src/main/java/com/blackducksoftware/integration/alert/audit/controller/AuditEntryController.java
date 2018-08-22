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
package com.blackducksoftware.integration.alert.audit.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.web.controller.BaseController;
import com.blackducksoftware.integration.alert.web.model.AlertPagedRestModel;

@RestController
@RequestMapping(BaseController.BASE_PATH + "/audit")
public class AuditEntryController extends BaseController {
    private final AuditEntryHandler auditEntryHandler;

    @Autowired
    public AuditEntryController(final AuditEntryHandler auditEntryHandler) {
        this.auditEntryHandler = auditEntryHandler;
    }

    @GetMapping
    public AlertPagedRestModel<AuditEntryRestModel> get(@RequestParam(value = "pageNumber", required = false) final Integer pageNumber, @RequestParam(value = "pageSize", required = false) final Integer pageSize,
            @RequestParam(value = "searchTerm", required = false) final String searchTerm, @RequestParam(value = "sortField", required = false) final String sortField,
            @RequestParam(value = "sortOrder", required = false) final String sortOrder) {
        if (StringUtils.isNotBlank(searchTerm)) {
            return auditEntryHandler.search(pageNumber, pageSize, searchTerm, sortField, sortOrder);
        } else {
            return auditEntryHandler.get(pageNumber, pageSize, sortField, sortOrder);
        }
    }

    @GetMapping(value = "/{id}")
    public AuditEntryRestModel get(@PathVariable(value = "id") final Long id) {
        return auditEntryHandler.get(id);
    }

    @PostMapping(value = "/{id}/resend")
    public ResponseEntity<String> post(@PathVariable(value = "id") final Long id) {
        return auditEntryHandler.resendNotification(id);
    }

}
