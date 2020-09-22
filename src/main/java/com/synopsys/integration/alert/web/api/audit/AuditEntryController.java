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
package com.synopsys.integration.alert.web.api.audit;

import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(AuditEntryController.AUDIT_BASE_PATH)
public class AuditEntryController extends BaseController {
    public static final String AUDIT_BASE_PATH = BaseController.BASE_PATH + "/audit";
    private final AuditEntryActions auditEntryActions;

    @Autowired
    public AuditEntryController(AuditEntryActions auditEntryActions) {
        this.auditEntryActions = auditEntryActions;
    }

    @GetMapping
    public AuditEntryPageModel getPage(@RequestParam(value = "pageNumber", required = false) Integer pageNumber, @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "searchTerm", required = false) String searchTerm, @RequestParam(value = "sortField", required = false) String sortField,
        @RequestParam(value = "sortOrder", required = false) String sortOrder, @RequestParam(value = "onlyShowSentNotifications", required = false) Boolean onlyShowSentNotifications) {
        ActionResponse<AuditEntryPageModel> response = auditEntryActions.get(pageNumber, pageSize, searchTerm, sortField, sortOrder, BooleanUtils.toBoolean(onlyShowSentNotifications));
        return ResponseFactory.createContentResponseFromAction(response);
    }

    @GetMapping(value = "/{id}")
    public AuditEntryModel get(@PathVariable(value = "id") Long id) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActions.get(id));
    }

    @GetMapping(value = "/job/{jobId}")
    public AuditJobStatusModel getAuditInfoForJob(@PathVariable(value = "jobId") UUID jobId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActions.getAuditInfoForJob(jobId));
    }

    @PostMapping(value = "/resend/{id}/")
    // TODO returning something other than the resource being interacted with is considered bad practice
    public AuditEntryPageModel resendById(@PathVariable(value = "id") Long notificationId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActions.resendNotification(notificationId, null));
    }

    @PostMapping(value = "/resend/{id}/job/{jobId}")
    // TODO returning something other than the resource being interacted with is considered bad practice
    public AuditEntryPageModel resendByIdAndJobId(@PathVariable(value = "id") Long notificationId, @PathVariable(value = "jobId") UUID jobId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActions.resendNotification(notificationId, jobId));
    }
}
