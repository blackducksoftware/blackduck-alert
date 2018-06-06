/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.audit.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelEventFactory;
import com.blackducksoftware.integration.hub.alert.datasource.AlertPage;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.AlertPagedRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ComponentRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.NotificationRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Transactional
@Component
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuditEntryRepositoryWrapper auditEntryRepository;
    private final AuditNotificationRepositoryWrapper auditNotificationRepository;
    private final NotificationManager notificationManager;
    private final CommonDistributionRepositoryWrapper commonDistributionRepository;
    private final ObjectTransformer objectTransformer;
    private final ChannelEventFactory<DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory;
    private final ProjectDataFactory projectDataFactory;
    private final ChannelTemplateManager channelTemplateManager;

    @Autowired
    public AuditEntryActions(final AuditEntryRepositoryWrapper auditEntryRepository, final NotificationManager notificationManager, final AuditNotificationRepositoryWrapper auditNotificationRepository,
            final CommonDistributionRepositoryWrapper commonDistributionRepository, final ObjectTransformer objectTransformer,
            final ChannelEventFactory<DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory, final ProjectDataFactory projectDataFactory,
            final ChannelTemplateManager channelTemplateManager) {
        this.auditEntryRepository = auditEntryRepository;
        this.notificationManager = notificationManager;
        this.auditNotificationRepository = auditNotificationRepository;
        this.commonDistributionRepository = commonDistributionRepository;
        this.objectTransformer = objectTransformer;
        this.channelEventFactory = channelEventFactory;
        this.projectDataFactory = projectDataFactory;
        this.channelTemplateManager = channelTemplateManager;
    }

    public AlertPagedRestModel<AuditEntryRestModel> get() {
        return get(null, null);
    }

    public AlertPagedRestModel<AuditEntryRestModel> get(final Integer pageNumber, final Integer pageSize) {
        AlertPage<AuditEntryEntity> auditEntries;
        logger.debug("Audit entry get. PageNumber: {} PageSize: {}", pageNumber, pageSize);
        if (pageNumber != null && pageSize != null) {
            final PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sort(Sort.Direction.DESC, "timeLastSent"));
            auditEntries = auditEntryRepository.findAll(pageRequest);
        } else {
            final List<AuditEntryEntity> contentList = auditEntryRepository.findAll();
            auditEntries = new AlertPage<>(1, 0, contentList.size(), contentList);
        }
        final AlertPagedRestModel<AuditEntryRestModel> pagedRestModel = createRestModels(auditEntries);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public AuditEntryRestModel get(final Long id) {
        if (id != null) {
            final AuditEntryEntity auditEntryEntity = auditEntryRepository.findById(id);
            if (auditEntryEntity != null) {
                return createRestModel(auditEntryEntity);
            }
        }
        return null;
    }

    public AlertPagedRestModel<AuditEntryRestModel> resendNotification(final Long id) throws IntegrationException, IllegalArgumentException {
        AuditEntryEntity auditEntryEntity = null;
        auditEntryEntity = auditEntryRepository.findById(id);
        if (auditEntryEntity == null) {
            throw new AlertException("No audit entry with the provided id exists.");
        }
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(relation -> relation.getNotificationId()).collect(Collectors.toList());
        final List<NotificationModel> notifications = notificationManager.findByIds(notificationIds);
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();
        final CommonDistributionConfigEntity commonConfigEntity = commonDistributionRepository.findById(commonConfigId);
        if (notifications == null || notifications.isEmpty()) {
            throw new IllegalArgumentException("The notification for this entry was purged. To edit the purge schedule, please see the Scheduling Configuration.");
        }
        if (commonConfigEntity == null) {
            throw new IllegalArgumentException("The job for this entry was deleted, can not re-send this entry.");
        }
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications);
        final DigestModel digestModel = new DigestModel(projectDataCollection);
        final ChannelEvent event = channelEventFactory.createEvent(commonConfigId, commonConfigEntity.getDistributionType(), digestModel);
        event.setAuditEntryId(auditEntryEntity.getId());
        channelTemplateManager.sendEvent(event);
        return get();
    }

    private AlertPagedRestModel<AuditEntryRestModel> createRestModels(final AlertPage<AuditEntryEntity> page) {
        return new AlertPagedRestModel<>(page.getTotalPages(), page.getCurrentPage(), page.getPageSize(), createRestModels(page.getContentList()));
    }

    private List<AuditEntryRestModel> createRestModels(final List<AuditEntryEntity> auditEntryEntities) {
        final List<AuditEntryRestModel> restModels = auditEntryEntities.stream().map(this::createRestModel).collect(Collectors.toList());
        return restModels;
    }

    private AuditEntryRestModel createRestModel(final AuditEntryEntity auditEntryEntity) {
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();

        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(relation -> relation.getNotificationId()).collect(Collectors.toList());
        final List<NotificationModel> notifications = notificationManager.findByIds(notificationIds);

        final CommonDistributionConfigEntity commonConfigEntity = commonDistributionRepository.findById(commonConfigId);

        final String id = objectTransformer.objectToString(auditEntryEntity.getId());
        final String timeCreated = objectTransformer.objectToString(auditEntryEntity.getTimeCreated());
        final String timeLastSent = objectTransformer.objectToString(auditEntryEntity.getTimeLastSent());

        String status = null;
        if (auditEntryEntity.getStatus() != null) {
            status = auditEntryEntity.getStatus().getDisplayName();
        }

        final String errorMessage = auditEntryEntity.getErrorMessage();
        final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

        NotificationRestModel notificationRestModel = null;
        if (!notifications.isEmpty() && notifications.get(0) != null) {
            try {
                notificationRestModel = objectTransformer.databaseEntityToConfigRestModel(notifications.get(0).getNotificationEntity(), NotificationRestModel.class);
                final Set<String> notificationTypes = notifications.stream().map(notification -> notification.getNotificationType().name()).collect(Collectors.toSet());
                notificationRestModel.setNotificationTypes(notificationTypes);
                final Set<ComponentRestModel> components = notifications.stream().map(notification -> new ComponentRestModel(notification.getComponentName(), notification.getComponentVersion(), notification.getPolicyRuleName(),
                        notification.getPolicyRuleUser())).collect(Collectors.toSet());
                notificationRestModel.setComponents(components);
            } catch (final AlertException e) {
                logger.error("Problem converting audit entry with id {}: {}", auditEntryEntity.getId(), e.getMessage());
            }
        }

        String distributionConfigName = null;
        String eventType = null;
        if (commonConfigEntity != null) {
            distributionConfigName = commonConfigEntity.getName();
            eventType = commonConfigEntity.getDistributionType();
        }

        return new AuditEntryRestModel(id, distributionConfigName, eventType, timeCreated, timeLastSent, status, errorMessage, errorStackTrace, notificationRestModel);
    }

}
