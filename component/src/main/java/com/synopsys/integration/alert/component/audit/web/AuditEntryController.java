/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.audit.web;

import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseController;
import com.synopsys.integration.alert.common.rest.model.AuditJobStatusesModel;
import com.synopsys.integration.alert.common.rest.model.JobIdsRequestModel;

@RestController
@RequestMapping(AuditEntryController.AUDIT_BASE_PATH)
public class AuditEntryController extends BaseController {
    public static final String AUDIT_BASE_PATH = AlertRestConstants.BASE_PATH + "/audit";
    private final AuditEntryActions auditEntryActions;

    @Autowired
    public AuditEntryController(AuditEntryActions auditEntryActions) {
        this.auditEntryActions = auditEntryActions;
    }

    @GetMapping
    public AuditEntryPageModel getPage(
        @RequestParam(value = "pageNumber", required = false) Integer pageNumber, @RequestParam(value = "pageSize", required = false) Integer pageSize,
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

    @PostMapping(value = "/job")
    public AuditJobStatusesModel queryForJobAuditInfoInJobs(@RequestBody JobIdsRequestModel queryRequestModel) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActions.queryForAuditInfoInJobs(queryRequestModel));
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
