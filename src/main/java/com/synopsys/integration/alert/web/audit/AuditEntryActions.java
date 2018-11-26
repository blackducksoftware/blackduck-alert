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
package com.synopsys.integration.alert.web.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
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
        return get(null, null, null, null, null);
    }

    public AlertPagedModel<AuditEntryModel> get(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder) {
        final List<AuditEntryModel> auditEntries = new ArrayList<>();

        final Page<NotificationContent> auditPage = queryForNotifications(sortField, sortOrder);
        final List<AuditEntryModel> auditEntryModels = createRestModels(auditPage.getContent());

        addMatchingModels(auditEntries, auditEntryModels, searchTerm);

        List<AuditEntryModel> pagedAuditEntries = auditEntries;
        int totalPages = 1;
        int pageNumberResponse = 1;
        if (null != pageSize && !auditEntries.isEmpty()) {
            pagedAuditEntries = new ArrayList<>();
            final int pageStart = pageNumber * pageSize;
            final int pageEnd = pageStart + pageSize;
            for (int i = 0; i < auditEntries.size(); i++) {
                if (i >= pageStart && i < pageEnd) {
                    pageNumberResponse = pageNumber;
                    pagedAuditEntries.add(auditEntries.get(i));
                }
            }
            final int count = auditEntries.size();
            final double division = (double) count / (double) pageSize;
            final double ceiling = Math.ceil(division);
            totalPages = (int) Math.round(ceiling);
        }
        final AlertPagedModel<AuditEntryModel> pagedRestModel = new AlertPagedModel<>(totalPages, pageNumberResponse, pagedAuditEntries.size(), pagedAuditEntries);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public AuditEntryModel get(final Long id) {
        if (id != null) {
            final Optional<NotificationContent> notificationContent = notificationManager.findById(id);
            if (notificationContent.isPresent()) {
                return createRestModel(notificationContent.get());
            }
        }
        return null;
    }

    public AlertPagedModel<AuditEntryModel> resendNotification(final Long id) throws IntegrationException {
        final Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(id);
        if (!auditEntryEntityOptional.isPresent()) {
            throw new AlertException("No audit entry with the provided id exists.");
        }

        final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.get();
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(AuditNotificationRelation::getNotificationId).collect(Collectors.toList());
        final List<NotificationContent> notifications = notificationManager.findByIds(notificationIds);
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();
        final Optional<? extends CommonDistributionConfig> optionalCommonConfig = jobConfigReader.getPopulatedConfig(commonConfigId);
        if (notifications == null || notifications.isEmpty()) {
            throw new AlertNotificationPurgedException("The notification for this entry was purged. To edit the purge schedule, please see the Scheduling Configuration.");
        }
        if (!optionalCommonConfig.isPresent()) {
            throw new AlertException("The job for this entry was deleted, can not re-send this entry.");
        }
        final CommonDistributionConfig commonConfig = optionalCommonConfig.get();
        final List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(commonConfig, notifications);
        distributionEvents.forEach(event -> {
            event.setAuditEntryId(auditEntryEntity.getId());
            channelTemplateManager.sendEvent(event);
        });
        return get();
    }

    private void addMatchingModels(final List<AuditEntryModel> listToAddTo, final List<AuditEntryModel> modelsToCheck, final String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            listToAddTo.addAll(modelsToCheck);
        } else {
            final String lowerCaseSearchTerm = searchTerm.toLowerCase(Locale.ENGLISH);
            for (final AuditEntryModel restModel : modelsToCheck) {
                if (null != restModel) {
                    if (doAnyJobsMatch(restModel.getJobs(), lowerCaseSearchTerm)) {
                        listToAddTo.add(restModel);
                    } else if (null != restModel.getNotification() && StringUtils.isNotBlank(restModel.getNotification().getContent()) && restModel.getNotification().getContent().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearchTerm)) {
                        listToAddTo.add(restModel);
                    }
                }
            }
        }
    }

    private boolean doAnyJobsMatch(final List<JobModel> jobs, final String lowerCaseSearchTerm) {
        for (final JobModel jobModel : jobs) {
            if (StringUtils.isNotBlank(jobModel.getName()) && jobModel.getName().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearchTerm)) {
                return true;
            } else if (StringUtils.isNotBlank(jobModel.getStatus()) && jobModel.getStatus().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearchTerm)) {
                return true;
            } else if (StringUtils.isNotBlank(jobModel.getTimeAuditCreated()) && jobModel.getTimeAuditCreated().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearchTerm)) {
                return true;
            } else if (StringUtils.isNotBlank(jobModel.getTimeLastSent()) && jobModel.getTimeLastSent().toLowerCase(Locale.ENGLISH).contains(lowerCaseSearchTerm)) {
                return true;
            }
        }
        return false;
    }

    private Page<NotificationContent> queryForNotifications(final String sortField, final String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "createdAt";
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(sortField) && "createdAt".equalsIgnoreCase(sortField) || "provider".equalsIgnoreCase(sortField) || "provider_creation_time".equalsIgnoreCase(sortField) || "notification_type".equalsIgnoreCase(sortField)
                || "content".equalsIgnoreCase(sortField)) {
            sortingField = sortField;
            sortQuery = true;
        }
        Sort.Direction sortingOrder = Sort.Direction.DESC;
        if (StringUtils.isNotBlank(sortOrder) && sortQuery) {
            if (Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                sortingOrder = Sort.Direction.ASC;
            }
        }
        logger.debug("Audit entry get. SortField: {} SortOrder: {}", sortingField, sortingOrder.name());
        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, new Sort(sortingOrder, sortingField));
        final Page<NotificationContent> auditPage = notificationManager.findAll(pageRequest);
        return auditPage;
    }

    private List<AuditEntryModel> createRestModels(final List<NotificationContent> notificationContentEntries) {
        final List<AuditEntryModel> auditEntryModels = notificationContentEntries.stream().map(this::createRestModel).collect(Collectors.toList());
        return auditEntryModels;
    }

    private AuditEntryModel createRestModel(final NotificationContent notificationContentEntry) {
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByNotificationId(notificationContentEntry.getId());
        final List<Long> auditEntryIds = relations.stream().map(AuditNotificationRelation::getAuditEntryId).collect(Collectors.toList());
        final List<AuditEntryEntity> auditEntryEntities = auditEntryRepository.findAllById(auditEntryIds);

        final List<JobModel> jobModels = new ArrayList<>();
        for (final AuditEntryEntity auditEntryEntity : auditEntryEntities) {
            final Long commonConfigId = auditEntryEntity.getCommonConfigId();

            final Optional<? extends CommonDistributionConfig> commonConfig = jobConfigReader.getPopulatedConfig(commonConfigId);

            final String id = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getId());
            final String timeCreated = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeCreated());
            final String timeLastSent = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeLastSent());

            String status = null;
            if (auditEntryEntity.getStatus() != null) {
                status = auditEntryEntity.getStatus().getDisplayName();
            }

            final String errorMessage = auditEntryEntity.getErrorMessage();
            final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

            String distributionConfigName = null;
            String eventType = null;
            if (commonConfig.isPresent()) {
                distributionConfigName = commonConfig.get().getName();
                eventType = commonConfig.get().getDistributionType();
            }

            jobModels.add(new JobModel(id, distributionConfigName, eventType, timeCreated, timeLastSent, status, errorMessage, errorStackTrace));
        }
        final String id = notificationContentConverter.getContentConverter().getStringValue(notificationContentEntry.getId());
        final NotificationConfig notificationConfig = (NotificationConfig) notificationContentConverter.populateConfigFromEntity(notificationContentEntry);

        return new AuditEntryModel(id, notificationConfig, jobModels);
    }
}
