/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.web;

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

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryModel;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseController;
import com.blackduck.integration.alert.common.rest.model.AuditJobStatusesModel;
import com.blackduck.integration.alert.common.rest.model.JobIdsRequestModel;

/**
 * @deprecated Replaced by AuditEntryController.
 * Deprecated in 7.x, To be removed in 9.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(AuditEntryControllerLegacy.AUDIT_BASE_PATH)
public class AuditEntryControllerLegacy extends BaseController {
    public static final String AUDIT_BASE_PATH = AlertRestConstants.BASE_PATH + "/audit";
    private final AuditEntryActionsLegacy auditEntryActionsLegacy;

    @Autowired
    public AuditEntryControllerLegacy(AuditEntryActionsLegacy auditEntryActionsLegacy) {
        this.auditEntryActionsLegacy = auditEntryActionsLegacy;
    }

    @GetMapping
    public AuditEntryPageModel getPage(
        @RequestParam(value = "pageNumber", required = false) Integer pageNumber, @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "searchTerm", required = false) String searchTerm, @RequestParam(value = "sortField", required = false) String sortField,
        @RequestParam(value = "sortOrder", required = false) String sortOrder, @RequestParam(value = "onlyShowSentNotifications", required = false) Boolean onlyShowSentNotifications) {
        ActionResponse<AuditEntryPageModel> response = auditEntryActionsLegacy.get(
            pageNumber,
            pageSize,
            searchTerm,
            sortField,
            sortOrder,
            BooleanUtils.toBoolean(onlyShowSentNotifications)
        );
        return ResponseFactory.createContentResponseFromAction(response);
    }

    @GetMapping(value = "/{id}")
    public AuditEntryModel get(@PathVariable(value = "id") Long id) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActionsLegacy.get(id));
    }

    @GetMapping(value = "/job/{jobId}")
    public AuditJobStatusModel getAuditInfoForJob(@PathVariable(value = "jobId") UUID jobId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActionsLegacy.getAuditInfoForJob(jobId));
    }

    @PostMapping(value = "/job")
    public AuditJobStatusesModel queryForJobAuditInfoInJobs(@RequestBody JobIdsRequestModel queryRequestModel) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActionsLegacy.queryForAuditInfoInJobs(queryRequestModel));
    }

    @PostMapping(value = "/resend/{id}/")
    // TODO returning something other than the resource being interacted with is considered bad practice
    public AuditEntryPageModel resendById(@PathVariable(value = "id") Long notificationId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActionsLegacy.resendNotification(notificationId, null));
    }

    @PostMapping(value = "/resend/{id}/job/{jobId}")
    // TODO returning something other than the resource being interacted with is considered bad practice
    public AuditEntryPageModel resendByIdAndJobId(@PathVariable(value = "id") Long notificationId, @PathVariable(value = "jobId") UUID jobId) {
        return ResponseFactory.createContentResponseFromAction(auditEntryActionsLegacy.resendNotification(notificationId, jobId));
    }

}
