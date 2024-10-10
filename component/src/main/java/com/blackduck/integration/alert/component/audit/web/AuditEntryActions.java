/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.web;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.processor.event.JobNotificationMappedEvent;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.audit.AuditDescriptorKey;

@Component
public class AuditEntryActions {
    private final AuditDescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final ProcessingFailedAccessor auditFailureAccessor;

    private final NotificationAccessor notificationAccessor;
    private final NotificationMappingProcessor notificationMappingProcessor;
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;

    private final EventManager eventManager;

    @Autowired
    public AuditEntryActions(
        AuditDescriptorKey descriptorKey,
        AuthorizationManager authorizationManager,
        ProcessingFailedAccessor auditFailureAccessor,
        NotificationAccessor notificationAccessor,
        NotificationMappingProcessor notificationMappingProcessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor,
        EventManager eventManager
    ) {
        this.descriptorKey = descriptorKey;
        this.authorizationManager = authorizationManager;
        this.auditFailureAccessor = auditFailureAccessor;
        this.notificationAccessor = notificationAccessor;
        this.notificationMappingProcessor = notificationMappingProcessor;
        this.jobNotificationMappingAccessor = jobNotificationMappingAccessor;
        this.eventManager = eventManager;
    }

    public ActionResponse<AuditEntryPageModel> get(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Integer page = ObjectUtils.defaultIfNull(pageNumber, AlertPagedModel.DEFAULT_PAGE_NUMBER);
        Integer size = ObjectUtils.defaultIfNull(pageSize, AlertPagedModel.DEFAULT_PAGE_SIZE);
        AuditEntryPageModel pagedRestModel = auditFailureAccessor.getPageOfAuditEntries(
            page,
            size,
            searchTerm,
            sortField,
            sortOrder
        );
        return new ActionResponse<>(HttpStatus.OK, pagedRestModel);
    }

    public ActionResponse<Void> resendNotification(Long notificationId) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        notificationAccessor.findById(notificationId)
            .ifPresent(notification -> {
                UUID correlationId = UUID.randomUUID();
                notificationMappingProcessor.processNotifications(correlationId, List.of(notification), List.of(FrequencyType.REAL_TIME));
                auditFailureAccessor.deleteAuditsWithNotificationId(notificationId);
                eventManager.sendEvent(new JobNotificationMappedEvent(correlationId));
            });
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    public ActionResponse<Void> resendNotification(Long notificationId, UUID jobConfigId) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        notificationAccessor.findById(notificationId)
            .ifPresent(notification -> {
                UUID correlationId = UUID.randomUUID();
                JobToNotificationMappingModel mappingModel = new JobToNotificationMappingModel(correlationId, jobConfigId, notificationId);
                jobNotificationMappingAccessor.addJobMappings(List.of(mappingModel));
                auditFailureAccessor.deleteAuditsWithJobIdAndNotificationId(jobConfigId, notificationId);
                eventManager.sendEvent(new JobNotificationMappedEvent(correlationId));
            });

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }
}
