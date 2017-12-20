/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelEventFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.AuditEntryRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.NotificationRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class AuditEntryHandler extends ControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryHandler.class);

    private final AuditEntryRepository auditEntryRepository;
    private final NotificationRepository notificationRepository;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ObjectTransformer objectTransformer;
    private final ChannelEventFactory<AbstractChannelEvent, DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory;
    private final ProjectDataFactory projectDataFactory;
    private final ChannelTemplateManager channelTemplateManager;

    @Autowired
    public AuditEntryHandler(final AuditEntryRepository auditEntryRepository, final NotificationRepository notificationRepository, final CommonDistributionRepository commonDistributionRepository, final ObjectTransformer objectTransformer,
            final ChannelEventFactory<AbstractChannelEvent, DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory, final ProjectDataFactory projectDataFactory,
            final ChannelTemplateManager channelTemplateManager) {
        super(objectTransformer);
        this.auditEntryRepository = auditEntryRepository;
        this.notificationRepository = notificationRepository;
        this.commonDistributionRepository = commonDistributionRepository;
        this.objectTransformer = objectTransformer;
        this.channelEventFactory = channelEventFactory;
        this.projectDataFactory = projectDataFactory;
        this.channelTemplateManager = channelTemplateManager;
    }

    public List<AuditEntryRestModel> get() {
        return createRestModels(auditEntryRepository.findAll());
    }

    public AuditEntryRestModel get(final Long id) {
        if (id != null) {
            final AuditEntryEntity auditEntryEntity = auditEntryRepository.findOne(id);
            if (auditEntryEntity != null) {
                return createRestModel(auditEntryEntity);
            }
        }
        return null;
    }

    public ResponseEntity<String> resendNotification(final Long id) {
        final AuditEntryEntity auditEntryEntity = auditEntryRepository.findOne(id);
        if (auditEntryEntity != null) {
            final NotificationEntity notificationEntity = notificationRepository.findOne(auditEntryEntity.getNotificationId());
            final Long commonConfigId = auditEntryEntity.getCommonConfigId();
            final CommonDistributionConfigEntity commonConfigEntity = commonDistributionRepository.findOne(commonConfigId);
            if (notificationEntity != null && commonConfigEntity != null) {
                final ProjectData projectData = projectDataFactory.createProjectData(notificationEntity);
                final AbstractChannelEvent event = channelEventFactory.createEvent(commonConfigId, commonConfigEntity.getDistributionType(), projectData);
                channelTemplateManager.sendEvent(event);
                return createResponse(HttpStatus.OK, id, "Attempting to resend notification...");
            }
            return createResponse(HttpStatus.GONE, id, "The notification for this entry was purged. To edit the purge schedule, please see the Scheduling Configuration.");
        }
        return createResponse(HttpStatus.BAD_REQUEST, id, "No audit entry with the provided id exists.");
    }

    private List<AuditEntryRestModel> createRestModels(final List<AuditEntryEntity> auditEntryEntities) {
        final List<AuditEntryRestModel> restModels = new ArrayList<>(auditEntryEntities.size());
        for (final AuditEntryEntity entity : auditEntryEntities) {
            restModels.add(createRestModel(entity));
        }
        return restModels;
    }

    private AuditEntryRestModel createRestModel(final AuditEntryEntity auditEntryEntity) {
        final Long notificationId = auditEntryEntity.getNotificationId();
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();

        final NotificationEntity notificationEntity = notificationRepository.findOne(notificationId);
        final CommonDistributionConfigEntity commonConfigEntity = commonDistributionRepository.findOne(commonConfigId);

        final String id = objectTransformer.objectToString(auditEntryEntity.getId());
        final String timeCreated = objectTransformer.objectToString(auditEntryEntity.getTimeCreated());
        final String timeLastSent = objectTransformer.objectToString(auditEntryEntity.getTimeLastSent());
        final String status = auditEntryEntity.getStatus().name();

        NotificationRestModel notificationRestModel = null;
        if (notificationEntity != null) {
            try {
                notificationRestModel = objectTransformer.databaseEntityToConfigRestModel(notificationEntity, NotificationRestModel.class);
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

        return new AuditEntryRestModel(id, distributionConfigName, eventType, timeCreated, timeLastSent, status, notificationRestModel);
    }

}
