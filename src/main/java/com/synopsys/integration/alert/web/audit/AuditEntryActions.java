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
package com.synopsys.integration.alert.web.audit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertJobMissingException;
import com.synopsys.integration.alert.common.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.processor.NotificationProcessor;
import com.synopsys.integration.exception.IntegrationException;

@Component
@Transactional
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private AuditUtility auditUtility;
    private NotificationManager notificationManager;
    private ConfigurationAccessor jobConfigReader;
    private ChannelEventManager eventManager;
    private NotificationProcessor notificationProcessor;

    @Autowired
    public AuditEntryActions(AuditUtility auditUtility, NotificationManager notificationManager, ConfigurationAccessor jobConfigReader, ChannelEventManager eventManager, NotificationProcessor notificationProcessor) {
        this.auditUtility = auditUtility;
        this.notificationManager = notificationManager;
        this.jobConfigReader = jobConfigReader;
        this.eventManager = eventManager;
        this.notificationProcessor = notificationProcessor;
    }

    public AlertPagedModel<AuditEntryModel> get() {
        return get(null, null, null, null, null, false);
    }

    public AlertPagedModel<AuditEntryModel> get(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications) {
        AlertPagedModel<AuditEntryModel> pagedRestModel = auditUtility.getPageOfAuditEntries(pageNumber, pageSize, searchTerm, sortField, sortOrder, onlyShowSentNotifications, auditUtility::convertToAuditEntryModelFromNotification);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public Optional<AuditEntryModel> get(Long id) {
        if (id != null) {
            Optional<AlertNotificationModel> notificationContent = notificationManager.findById(id);
            return notificationContent.map(auditUtility::convertToAuditEntryModelFromNotification);
        }
        return Optional.empty();
    }

    public Optional<AuditJobStatusModel> getAuditInfoForJob(UUID jobId) {
        if (jobId != null) {
            return auditUtility.findFirstByJobId(jobId);
        }
        return Optional.empty();
    }

    public AlertPagedModel<AuditEntryModel> resendNotification(Long notificationId, UUID commonConfigId) throws IntegrationException {
        AlertNotificationModel notificationContent = notificationManager
                                                         .findById(notificationId)
                                                         .orElseThrow(() -> new AlertNotificationPurgedException("No notification with this id exists."));
        List<DistributionEvent> distributionEvents;
        if (null != commonConfigId) {
            ConfigurationJobModel commonDistributionConfig = jobConfigReader.getJobById(commonConfigId).orElseThrow(() -> {
                logger.warn("The Distribution Job with Id {} could not be found. This notification could not be sent", commonConfigId);
                return new AlertJobMissingException("The Distribution Job with this id could not be found.", commonConfigId);
            });
            if (commonDistributionConfig.isEnabled()) {
                distributionEvents = notificationProcessor.processNotifications(commonDistributionConfig, List.of(notificationContent));
            } else {
                UUID jobConfigId = commonDistributionConfig.getJobId();
                logger.warn("The Distribution Job with Id {} was disabled. This notification could not be sent", jobConfigId);
                throw new AlertJobMissingException("The Distribution Job is currently disabled.", jobConfigId);
            }
        } else {
            distributionEvents = notificationProcessor.processNotifications(List.of(notificationContent));
        }
        if (distributionEvents.isEmpty()) {
            logger.warn("This notification could not be sent. Make sure you have a Distribution Job configured to handle this notification.");
        }

        for (DistributionEvent event : distributionEvents) {
            UUID commonDistributionId = UUID.fromString(event.getConfigId());
            Long auditId = auditUtility.findMatchingAuditId(notificationContent.getId(), commonDistributionId).orElse(null);
            Map<Long, Long> notificationIdToAuditId = new HashMap<>();
            notificationIdToAuditId.put(notificationContent.getId(), auditId);
            event.setNotificationIdToAuditId(notificationIdToAuditId);
            eventManager.sendEvent(event);
        }
        return get();
    }

}
