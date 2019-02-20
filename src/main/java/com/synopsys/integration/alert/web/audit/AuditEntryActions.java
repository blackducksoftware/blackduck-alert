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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.channel.ChannelTemplateManager;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.exception.AlertJobMissingException;
import com.synopsys.integration.alert.web.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.alert.web.model.NotificationConfig;
import com.synopsys.integration.alert.web.model.NotificationContentConverter;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;
import com.synopsys.integration.exception.IntegrationException;

@Component
@Transactional
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final NotificationManager notificationManager;
    private final JobConfigReader jobConfigReader;
    private final NotificationContentConverter notificationContentConverter;
    private final ChannelTemplateManager channelTemplateManager;
    private final NotificationProcessor notificationProcessor;

    @Autowired
    public AuditEntryActions(final AuditEntryRepository auditEntryRepository, final NotificationManager notificationManager, final AuditNotificationRepository auditNotificationRepository,
        final JobConfigReader jobConfigReader, final NotificationContentConverter notificationContentConverter, final ChannelTemplateManager channelTemplateManager, final NotificationProcessor notificationProcessor) {
        this.auditEntryRepository = auditEntryRepository;
        this.notificationManager = notificationManager;
        this.auditNotificationRepository = auditNotificationRepository;
        this.jobConfigReader = jobConfigReader;
        this.notificationContentConverter = notificationContentConverter;
        this.channelTemplateManager = channelTemplateManager;
        this.notificationProcessor = notificationProcessor;
    }

    public AlertPagedModel<AuditEntryModel> get() {
        return get(null, null, null, null, null, false);
    }

    public AlertPagedModel<AuditEntryModel> get(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder, final boolean onlyShowSentNotifications) {
        final Page<NotificationContent> auditPage = queryForNotifications(sortField, sortOrder, searchTerm, pageNumber, pageSize, onlyShowSentNotifications);
        final List<AuditEntryModel> auditEntries = createRestModels(auditPage.getContent(), sortField, sortOrder);
        final AlertPagedModel<AuditEntryModel> pagedRestModel = new AlertPagedModel<>(auditPage.getTotalPages(), auditPage.getNumber(), auditEntries.size(), auditEntries);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public Optional<AuditEntryModel> get(final Long id) {
        if (id != null) {
            final Optional<NotificationContent> notificationContent = notificationManager.findById(id);
            return notificationContent.map(this::createRestModel);
        }
        return Optional.empty();
    }

    public Optional<AuditJobStatusModel> getAuditInfoForJob(final UUID jobId) {
        if (jobId != null) {
            final Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(jobId);
            return auditEntryEntity.map(value -> {
                String timeCreated = null;
                if (null != value.getTimeCreated()) {
                    timeCreated = notificationContentConverter.getContentConverter().getStringValue(value.getTimeCreated());
                }
                String timeLastSent = null;
                if (null != value.getTimeLastSent()) {
                    timeLastSent = notificationContentConverter.getContentConverter().getStringValue(value.getTimeLastSent());
                }
                String status = null;
                if (null != value.getStatus()) {
                    status = value.getStatus();
                }
                return new AuditJobStatusModel(timeCreated, timeLastSent, status);
            });
        }
        return Optional.empty();
    }

    public AlertPagedModel<AuditEntryModel> resendNotification(final Long notificationId, final UUID commonConfigId) throws IntegrationException {
        final NotificationContent notificationContent = notificationManager
                                                            .findById(notificationId)
                                                            .orElseThrow(() -> new AlertNotificationPurgedException("No notification with this id exists."));
        final List<DistributionEvent> distributionEvents;
        if (null != commonConfigId) {
            final CommonDistributionConfiguration commonDistributionConfig = jobConfigReader.getPopulatedJobConfig(commonConfigId).orElseThrow(() -> {
                logger.warn("The Distribution Job with Id {} could not be found. This notification could not be sent", commonConfigId);
                return new AlertJobMissingException("The Distribution Job with this id could not be found.", commonConfigId);
            });
            distributionEvents = notificationProcessor.processNotifications(commonDistributionConfig, List.of(notificationContent));
        } else {
            distributionEvents = notificationProcessor.processNotifications(List.of(notificationContent));
        }
        if (distributionEvents.isEmpty()) {
            logger.warn("This notification could not be sent. Make sure you have a Distribution Job configured to handle this notification.");
        }
        distributionEvents.forEach(event -> {
            final UUID commonDistributionId = UUID.fromString(event.getConfigId());
            Long auditId = null;
            final Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findMatchingAudit(notificationContent.getId(), commonDistributionId);
            final Map<Long, Long> notificationIdToAuditId = new HashMap<>();
            if (auditEntryEntity.isPresent()) {
                auditId = auditEntryEntity.get().getId();
            }
            notificationIdToAuditId.put(notificationContent.getId(), auditId);
            event.setNotificationIdToAuditId(notificationIdToAuditId);
            channelTemplateManager.sendEvent(event);
        });
        return get();
    }

    private Page<NotificationContent> queryForNotifications(final String sortField, final String sortOrder, final String searchTerm, final Integer pageNumber, final Integer pageSize, final boolean onlyShowSentNotifications) {
        final PageRequest pageRequest = notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);
        final Page<NotificationContent> auditPage;
        if (StringUtils.isNotBlank(searchTerm)) {
            auditPage = notificationManager.findAllWithSearch(searchTerm, pageRequest, onlyShowSentNotifications);
        } else {
            auditPage = notificationManager.findAll(pageRequest, onlyShowSentNotifications);
        }
        return auditPage;
    }

    private List<AuditEntryModel> createRestModels(final List<NotificationContent> notificationContentEntries, final String sortField, final String sortOrder) {
        final List<AuditEntryModel> auditEntryModels = notificationContentEntries.stream().map(this::createRestModel).collect(Collectors.toList());
        if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent") || sortField.equalsIgnoreCase("overallStatus")) {
            // We do this sorting here because lastSent is not a field in the NotificationContent entity and overallStatus is not stored in the database
            boolean ascendingOrder = false;
            if (StringUtils.isNotBlank(sortOrder) && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                ascendingOrder = true;
            }
            Comparator comparator;
            if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent")) {
                final Function<AuditEntryModel, Date> function = auditEntryModel -> {
                    Date date = null;
                    if (StringUtils.isNotBlank(auditEntryModel.getLastSent())) {
                        date = notificationContentConverter.parseDateString(auditEntryModel.getLastSent());
                    }
                    return date;
                };
                comparator = Comparator.comparing(function, Comparator.nullsFirst(Comparator.reverseOrder()));
            } else {
                comparator = Comparator.comparing(AuditEntryModel::getOverallStatus, Comparator.nullsLast(String::compareTo));
            }
            if (ascendingOrder) {
                comparator = comparator.reversed();
            }
            auditEntryModels.sort(comparator);
        }
        return auditEntryModels;
    }

    private AuditEntryModel createRestModel(final NotificationContent notificationContentEntry) {
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByNotificationId(notificationContentEntry.getId());
        final List<Long> auditEntryIds = relations.stream().map(AuditNotificationRelation::getAuditEntryId).collect(Collectors.toList());
        final List<AuditEntryEntity> auditEntryEntities = auditEntryRepository.findAllById(auditEntryIds);

        AuditEntryStatus overallStatus = null;
        String lastSent = null;
        Date lastSentDate = null;
        final List<JobAuditModel> jobAuditModels = new ArrayList<>();
        for (final AuditEntryEntity auditEntryEntity : auditEntryEntities) {
            final UUID commonConfigId = auditEntryEntity.getCommonConfigId();

            if (null == lastSentDate || (null != auditEntryEntity.getTimeLastSent() && lastSentDate.before(auditEntryEntity.getTimeLastSent()))) {
                lastSentDate = auditEntryEntity.getTimeLastSent();
                lastSent = notificationContentConverter.getContentConverter().getStringValue(lastSentDate);
            }
            final String id = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getId());
            final String configId = notificationContentConverter.getContentConverter().getStringValue(commonConfigId);
            final String timeCreated = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeCreated());
            final String timeLastSent = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeLastSent());

            AuditEntryStatus status = null;
            if (auditEntryEntity.getStatus() != null) {
                status = AuditEntryStatus.valueOf(auditEntryEntity.getStatus());
                overallStatus = getWorstStatus(overallStatus, status);
            }

            final String errorMessage = auditEntryEntity.getErrorMessage();
            final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

            final Optional<CommonDistributionConfiguration> commonConfig = jobConfigReader.getPopulatedJobConfig(commonConfigId);
            String distributionConfigName = null;
            String eventType = null;
            if (commonConfig.isPresent()) {
                distributionConfigName = commonConfig.get().getName();
                eventType = commonConfig.get().getChannelName();
            }

            String statusDisplayName = null;
            if (null != status) {
                statusDisplayName = status.getDisplayName();
            }
            final AuditJobStatusModel auditJobStatusModel = new AuditJobStatusModel(timeCreated, timeLastSent, statusDisplayName);
            jobAuditModels.add(new JobAuditModel(id, configId, distributionConfigName, eventType, auditJobStatusModel, errorMessage, errorStackTrace));
        }
        final String id = notificationContentConverter.getContentConverter().getStringValue(notificationContentEntry.getId());
        final NotificationConfig notificationConfig = (NotificationConfig) notificationContentConverter.populateConfigFromEntity(notificationContentEntry);

        String overallStatusDisplayName = null;
        if (null != overallStatus) {
            overallStatusDisplayName = overallStatus.getDisplayName();
        }
        return new AuditEntryModel(id, notificationConfig, jobAuditModels, overallStatusDisplayName, lastSent);
    }

    private AuditEntryStatus getWorstStatus(final AuditEntryStatus overallStatus, final AuditEntryStatus currentStatus) {
        AuditEntryStatus newOverallStatus = overallStatus;
        if (null == overallStatus || currentStatus == AuditEntryStatus.FAILURE || (AuditEntryStatus.SUCCESS == overallStatus && AuditEntryStatus.SUCCESS != currentStatus)) {
            newOverallStatus = currentStatus;
        }

        return newOverallStatus;
    }
}
