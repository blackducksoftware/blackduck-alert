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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.controller.handler.ControllerHandler;
import com.synopsys.integration.alert.web.exception.AlertJobMissingException;
import com.synopsys.integration.alert.web.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AuditEntryHandler extends ControllerHandler {
    private final Gson gson;
    private final AuditEntryActions auditEntryActions;

    @Autowired
    public AuditEntryHandler(final ContentConverter contentConverter, final Gson gson, final AuditEntryActions auditEntryActions) {
        super(contentConverter);
        this.gson = gson;
        this.auditEntryActions = auditEntryActions;
    }

    public AlertPagedModel<AuditEntryModel> get(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder, final boolean onlyShowSentNotifications) {
        return auditEntryActions.get(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications);
    }

    public ResponseEntity<String> get(final Long id) {
        final Optional<AuditEntryModel> auditEntryModel = auditEntryActions.get(id);
        if (auditEntryModel.isPresent()) {
            return createResponse(HttpStatus.OK, id, gson.toJson(auditEntryModel.get()));
        } else {
            return createResponse(HttpStatus.GONE, id, "This Audit entry could not be found.");
        }
    }

    public ResponseEntity<String> getAuditInfoForJob(final UUID jobId) {
        final Optional<JobAuditModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(jobId);
        if (jobAuditModel.isPresent()) {
            return createResponse(HttpStatus.OK, jobId.toString(), gson.toJson(jobAuditModel.get()));
        } else {
            return createResponse(HttpStatus.GONE, jobId.toString(), "The Audit information could not be found for this job.");
        }
    }

    public ResponseEntity<String> resendNotification(final Long notificationId, final UUID commonConfigId) {
        AlertPagedModel<AuditEntryModel> auditEntries = null;
        try {
            auditEntries = auditEntryActions.resendNotification(notificationId, commonConfigId);
            return createResponse(HttpStatus.OK, notificationId, gson.toJson(auditEntries));
        } catch (final AlertNotificationPurgedException e) {
            return createResponse(HttpStatus.GONE, notificationId, e.getMessage());
        } catch (final AlertJobMissingException e) {
            return createResponse(HttpStatus.GONE, commonConfigId.toString(), e.getMessage());
        } catch (final IntegrationException e) {
            return createResponse(HttpStatus.BAD_REQUEST, notificationId, e.getMessage());
        }
    }

}
