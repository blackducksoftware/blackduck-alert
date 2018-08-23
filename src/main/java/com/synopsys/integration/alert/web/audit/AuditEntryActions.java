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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.ChannelTemplateManager;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.exception.AlertNotificationPurgedException;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.alert.web.model.NotificationConfig;
import com.synopsys.integration.alert.web.model.NotificationContentConverter;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.exception.IntegrationException;

@Transactional
@Component
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final NotificationManager notificationManager;
    private final CommonDistributionRepository commonDistributionRepository;
    private final NotificationContentConverter notificationContentConverter;
    private final ChannelEventFactory channelEventFactory;
    private final ChannelTemplateManager channelTemplateManager;

    @Autowired
    public AuditEntryActions(final AuditEntryRepository auditEntryRepository, final NotificationManager notificationManager, final AuditNotificationRepository auditNotificationRepository,
    final CommonDistributionRepository commonDistributionRepository, final NotificationContentConverter notificationContentConverter,
    final ChannelEventFactory channelEventFactory, final ChannelTemplateManager channelTemplateManager) {
        this.auditEntryRepository = auditEntryRepository;
        this.notificationManager = notificationManager;
        this.auditNotificationRepository = auditNotificationRepository;
        this.commonDistributionRepository = commonDistributionRepository;
        this.notificationContentConverter = notificationContentConverter;
        this.channelEventFactory = channelEventFactory;
        this.channelTemplateManager = channelTemplateManager;
    }

    public AlertPagedModel<AuditEntryConfig> get() {
        return get(null, null, null, null, null);
    }

    public AlertPagedModel<AuditEntryConfig> get(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder) {
        List<AuditEntryConfig> auditEntries = new ArrayList<>();

        final Page<AuditEntryEntity> auditPage = queryForAuditEntries(sortField, sortOrder);
        final List<AuditEntryConfig> auditEntryConfigs = createRestModels(auditPage.getContent());

        addMatchingModels(auditEntries, auditEntryConfigs, searchTerm);
        auditEntries = sortRestModels(auditEntries, sortField, sortOrder);

        List<AuditEntryConfig> pagedAuditEntries = auditEntries;
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
        final AlertPagedModel<AuditEntryConfig> pagedRestModel = new AlertPagedModel<>(totalPages, pageNumberResponse, pagedAuditEntries.size(), pagedAuditEntries);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public AuditEntryConfig get(final Long id) {
        if (id != null) {
            final Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findById(id);
            if (auditEntryEntity.isPresent()) {
                return createRestModel(auditEntryEntity.get());
            }
        }
        return null;
    }

    private void addMatchingModels(final List<AuditEntryConfig> listToAddTo, final List<AuditEntryConfig> modelsToCheck, final String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            listToAddTo.addAll(modelsToCheck);
        } else {
            for (final AuditEntryConfig restModel : modelsToCheck) {
                if (restModel.getName().contains(searchTerm) || restModel.getStatus().contains(searchTerm) || restModel.getTimeCreated().contains(searchTerm) || restModel.getTimeLastSent().contains(searchTerm)) {
                    listToAddTo.add(restModel);
                } else if (null != restModel.getNotification() && restModel.getNotification().getContent().contains(searchTerm)) {
                    listToAddTo.add(restModel);
                }
            }
        }
    }

    private Page<AuditEntryEntity> queryForAuditEntries(final String sortField, final String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "timeLastSent";
        // We can only modify the query for the fields that exist in AuditEntryEntity
        if (StringUtils.isNotBlank(sortField) && "timeCreated".equalsIgnoreCase(sortField) || "timeLastSent".equalsIgnoreCase(sortField) || "status".equalsIgnoreCase(sortField)) {
            sortingField = sortField;
            sortQuery = true;
        }
        Sort.Direction sortingOrder = Sort.Direction.DESC;
        if (StringUtils.isNotBlank(sortOrder) && sortQuery) {
            if (Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                sortingOrder = Sort.Direction.ASC;
            }
        }
        //TODO change log back to debug
        logger.error("Audit entry get. SortField: {} SortOrder: {}", sortingField, sortingOrder.name());
        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, new Sort(sortingOrder, sortingField));
        final Page<AuditEntryEntity> auditPage = auditEntryRepository.findAll(pageRequest);
        return auditPage;
    }

    public AlertPagedModel<AuditEntryConfig> resendNotification(final Long id) throws IntegrationException {
        final Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(id);
        if (!auditEntryEntityOptional.isPresent()) {
            throw new AlertException("No audit entry with the provided id exists.");
        }

        final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.get();
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(AuditNotificationRelation::getNotificationId).collect(Collectors.toList());
        final List<NotificationContent> notifications = notificationManager.findByIds(notificationIds);
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();
        final Optional<CommonDistributionConfigEntity> commonConfigEntity = commonDistributionRepository.findById(commonConfigId);
        if (notifications == null || notifications.isEmpty()) {
            throw new AlertNotificationPurgedException("The notification for this entry was purged. To edit the purge schedule, please see the Scheduling Configuration.");
        }
        if (!commonConfigEntity.isPresent()) {
            throw new AlertException("The job for this entry was deleted, can not re-send this entry.");
        }
        notifications.forEach(notificationContent -> {
            final ChannelEvent event = channelEventFactory.createChannelEvent(commonConfigId, commonConfigEntity.get().getDistributionType(), notificationContent);
            event.setAuditEntryId(auditEntryEntity.getId());
            channelTemplateManager.sendEvent(event);
        });
        return get();
    }

    private List<AuditEntryConfig> sortRestModels(final List<AuditEntryConfig> auditEntryConfigs, final String sortField, final String sortOrder) {
        // We can only want to sort for the fields that could not be sorted by in the query for AuditEntryEntity
        if (StringUtils.isNotBlank(sortField) && (sortField.equalsIgnoreCase("name") || sortField.equalsIgnoreCase("notificationProviderName"))) {
            //TODO remove this log
            logger.error("SORTING BY {} ORDER {}", sortField, sortOrder);
            final boolean sortByName = sortField.equalsIgnoreCase("name");
            boolean ascendingOrder = false;
            if (StringUtils.isNotBlank(sortOrder) && "asc".equalsIgnoreCase(sortOrder)) {
                ascendingOrder = true;
            }
            Comparator comparator;
            if (sortByName) {
                comparator = Comparator.comparing(AuditEntryConfig::getName);
            } else {
                final Function<AuditEntryConfig, String> function = audit -> audit.getNotification().getProvider();
                comparator = Comparator.comparing(function);
            }
            if (ascendingOrder) {
                comparator = comparator.reversed();
            }
            auditEntryConfigs.sort(comparator);
        }

        return auditEntryConfigs;
    }

    private List<AuditEntryConfig> createRestModels(final List<AuditEntryEntity> auditEntryEntities) {
        final List<AuditEntryConfig> auditEntryConfigs = auditEntryEntities.stream().map(this::createRestModel).collect(Collectors.toList());
        return auditEntryConfigs;
    }

    private AuditEntryConfig createRestModel(final AuditEntryEntity auditEntryEntity) {
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();

        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(AuditNotificationRelation::getNotificationId).collect(Collectors.toList());
        final List<NotificationContent> notifications = notificationManager.findByIds(notificationIds);

        final Optional<CommonDistributionConfigEntity> commonConfigEntity = commonDistributionRepository.findById(commonConfigId);

        final String id = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getId());
        final String timeCreated = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeCreated());
        final String timeLastSent = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeLastSent());

        String status = null;
        if (auditEntryEntity.getStatus() != null) {
            status = auditEntryEntity.getStatus().getDisplayName();
        }

        final String errorMessage = auditEntryEntity.getErrorMessage();
        final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

        NotificationConfig notificationConfig = null;
        if (!notifications.isEmpty() && notifications.get(0) != null) {
            notificationConfig = (NotificationConfig) notificationContentConverter.populateConfigFromEntity(notifications.get(0));
        }

        String distributionConfigName = null;
        String eventType = null;
        if (commonConfigEntity.isPresent()) {
            distributionConfigName = commonConfigEntity.get().getName();
            eventType = commonConfigEntity.get().getDistributionType();
        }

        return new AuditEntryConfig(id, distributionConfigName, eventType, timeCreated, timeLastSent, status, errorMessage, errorStackTrace, notificationConfig);
    }

}
