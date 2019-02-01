/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.audit;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.alert.web.exception.AlertJobMissingException;
import com.synopsys.integration.alert.web.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.exception.IntegrationException;

@RestController
@RequestMapping(BaseController.BASE_PATH + "/audit")
public class AuditEntryController extends BaseController {
    private final AuditEntryActions auditEntryActions;
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;

    @Autowired
    public AuditEntryController(final AuditEntryActions auditEntryActions, final ContentConverter contentConverter, final ResponseFactory responseFactory) {
        this.auditEntryActions = auditEntryActions;
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public AlertPagedModel<AuditEntryModel> get(@RequestParam(value = "pageNumber", required = false) final Integer pageNumber, @RequestParam(value = "pageSize", required = false) final Integer pageSize,
        @RequestParam(value = "searchTerm", required = false) final String searchTerm, @RequestParam(value = "sortField", required = false) final String sortField,
        @RequestParam(value = "sortOrder", required = false) final String sortOrder, @RequestParam(value = "onlyShowSentNotifications", required = false) final Boolean onlyShowSentNotifications) {
        return auditEntryActions.get(pageNumber, pageSize, searchTerm, sortField, sortOrder, BooleanUtils.toBoolean(onlyShowSentNotifications));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> get(@PathVariable(value = "id") final Long id) {
        final Optional<AuditEntryModel> auditEntryModel = auditEntryActions.get(id);
        final String stringId = contentConverter.getStringValue(id);
        if (auditEntryModel.isPresent()) {
            return responseFactory.createOkResponse(stringId, contentConverter.getJsonString(auditEntryModel.get()));
        } else {
            return responseFactory.createGoneResponse(stringId, "This Audit entry could not be found.");
        }
    }

    @GetMapping(value = "/job/{jobId}")
    public ResponseEntity<String> getAuditInfoForJob(@PathVariable(value = "jobId") final UUID jobId) {
        final Optional<AuditJobStatusModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(jobId);
        final String jobIdString = jobId.toString();
        if (jobAuditModel.isPresent()) {
            return responseFactory.createOkResponse(jobIdString, contentConverter.getJsonString(jobAuditModel.get()));
        } else {
            return responseFactory.createGoneResponse(jobIdString, "The Audit information could not be found for this job.");
        }
    }

    @PostMapping(value = "/resend/{id}/")
    public ResponseEntity<String> post(@PathVariable(value = "id") final Long notificationId) {
        return resendNotification(notificationId, null);
    }

    @PostMapping(value = "/resend/{id}/job/{jobId}")
    public ResponseEntity<String> post(@PathVariable(value = "id") final Long notificationId, @PathVariable(value = "jobId") final UUID jobId) {
        return resendNotification(notificationId, jobId);
    }

    private ResponseEntity<String> resendNotification(final Long notificationId, final UUID commonConfigId) {
        final String stringNotificationId = contentConverter.getStringValue(notificationId);
        try {
            final AlertPagedModel<AuditEntryModel> auditEntries = auditEntryActions.resendNotification(notificationId, commonConfigId);
            return responseFactory.createOkResponse(stringNotificationId, contentConverter.getJsonString(auditEntries));
        } catch (final AlertNotificationPurgedException e) {
            return responseFactory.createGoneResponse(stringNotificationId, e.getMessage());
        } catch (final AlertJobMissingException e) {
            return responseFactory.createGoneResponse(commonConfigId.toString(), e.getMessage());
        } catch (final IntegrationException e) {
            return responseFactory.createBadRequestResponse(stringNotificationId, e.getMessage());
        }
    }

}
