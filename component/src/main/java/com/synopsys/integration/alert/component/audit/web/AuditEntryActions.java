/**
 * component
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
package com.synopsys.integration.alert.component.audit.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;

@Component
@Transactional
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuthorizationManager authorizationManager;
    private final AuditDescriptorKey descriptorKey;
    private final NotificationAccessor notificationAccessor;
    private final AuditAccessor auditAccessor;
    private final JobAccessor jobAccessor;
    private final ChannelEventManager eventManager;
    private final NotificationProcessor notificationProcessor;

    @Autowired
    public AuditEntryActions(
        AuthorizationManager authorizationManager,
        AuditDescriptorKey descriptorKey,
        AuditAccessor auditAccessor,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor,
        ChannelEventManager eventManager,
        NotificationProcessor notificationProcessor
    ) {
        this.authorizationManager = authorizationManager;
        this.descriptorKey = descriptorKey;
        this.auditAccessor = auditAccessor;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
        this.eventManager = eventManager;
        this.notificationProcessor = notificationProcessor;
    }

    public ActionResponse<AuditEntryPageModel> get() {
        return get(null, null, null, null, null, false);
    }

    public ActionResponse<AuditEntryPageModel> get(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Integer page = ObjectUtils.defaultIfNull(pageNumber, 0);
        Integer size = ObjectUtils.defaultIfNull(pageSize, 100);
        AuditEntryPageModel pagedRestModel = auditAccessor.getPageOfAuditEntries(page, size, searchTerm, sortField, sortOrder, onlyShowSentNotifications, auditAccessor::convertToAuditEntryModelFromNotification);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return new ActionResponse<>(HttpStatus.OK, pagedRestModel);
    }

    public ActionResponse<AuditEntryModel> get(Long id) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Optional<AlertNotificationModel> notificationContent = notificationAccessor.findById(id);
        if (notificationContent.isPresent()) {
            AuditEntryModel auditEntryModel = auditAccessor.convertToAuditEntryModelFromNotification(notificationContent.get());
            return new ActionResponse<>(HttpStatus.OK, auditEntryModel);
        }
        return new ActionResponse<>(HttpStatus.GONE, "This Audit entry could not be found.");
    }

    public ActionResponse<AuditJobStatusModel> getAuditInfoForJob(UUID jobId) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Optional<AuditJobStatusModel> auditJobStatusModel = auditAccessor.findFirstByJobId(jobId);
        if (auditJobStatusModel.isPresent()) {
            return new ActionResponse<>(HttpStatus.OK, auditJobStatusModel.get());
        }
        return new ActionResponse<>(HttpStatus.GONE, "The Audit information could not be found for this job.");
    }

    public ActionResponse<AuditEntryPageModel> resendNotification(Long notificationId, UUID commonConfigId) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        Optional<AlertNotificationModel> notification = notificationAccessor
                                                            .findById(notificationId);
        if (notification.isEmpty()) {
            return new ActionResponse<>(HttpStatus.GONE, "No notification with this id exists.");
        }
        AlertNotificationModel notificationContent = notification.get();

        List<DistributionEvent> distributionEvents;
        if (null != commonConfigId) {
            Optional<ConfigurationJobModel> commonDistributionConfig = jobAccessor.getJobById(commonConfigId);
            if (commonDistributionConfig.isEmpty()) {
                String message = String.format("The Distribution Job with this id could not be found. %s", commonConfigId.toString());
                return new ActionResponse<>(HttpStatus.GONE, message);
            }
            ConfigurationJobModel commonConfig = commonDistributionConfig.get();
            if (commonConfig.isEnabled()) {
                distributionEvents = notificationProcessor.processNotificationsForJob(commonConfig, List.of(notificationContent));
            } else {
                UUID jobConfigId = commonConfig.getJobId();
                logger.warn("The Distribution Job with Id {} was disabled. This notification could not be sent", jobConfigId);
                String message = String.format("The Distribution Job is currently disabled. %s", jobConfigId.toString());
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, message);
            }
        } else {
            distributionEvents = notificationProcessor.processNotifications(List.of(notificationContent));
        }
        if (distributionEvents.isEmpty()) {
            logger.warn("This notification could not be sent. Make sure you have a Distribution Job configured to handle this notification.");
        }

        for (DistributionEvent event : distributionEvents) {
            UUID commonDistributionId = UUID.fromString(event.getConfigId());
            Long auditId = auditAccessor.findMatchingAuditId(notificationContent.getId(), commonDistributionId).orElse(null);
            Map<Long, Long> notificationIdToAuditId = new HashMap<>();
            notificationIdToAuditId.put(notificationContent.getId(), auditId);
            event.setNotificationIdToAuditId(notificationIdToAuditId);
            eventManager.sendEvent(event);
        }
        return get();
    }

}
