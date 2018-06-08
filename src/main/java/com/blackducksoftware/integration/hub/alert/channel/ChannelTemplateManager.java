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
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepository;
import com.blackducksoftware.integration.hub.alert.audit.repository.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.google.gson.Gson;

@Transactional
@Component
public class ChannelTemplateManager {
    private final Gson gson;
    private final JmsTemplate jmsTemplate;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final AlertEventContentConverter contentConverter;

    @Autowired
    public ChannelTemplateManager(final Gson gson, final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository, final JmsTemplate jmsTemplate,
            final AlertEventContentConverter contentConverter) {
        this.gson = gson;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.jmsTemplate = jmsTemplate;
        this.contentConverter = contentConverter;
    }

    public void sendEvents(final List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(event -> {
                sendEvent(event);
            });
        }
    }

    public boolean sendEvent(final AlertEvent event) {
        final String destination = event.getDestination();
        if (event instanceof ChannelEvent) {
            final ChannelEvent channelEvent = (ChannelEvent) event;
            Optional<AuditEntryEntity> auditEntryEntity = null;
            if (channelEvent.getAuditEntryId() == null) {
                auditEntryEntity = Optional.of(new AuditEntryEntity(channelEvent.getCommonDistributionConfigId(), new Date(System.currentTimeMillis()), null, null, null, null));
            } else {
                auditEntryEntity = auditEntryRepository.findById(channelEvent.getAuditEntryId());
            }
            auditEntryEntity.get().setStatus(StatusEnum.PENDING);
            final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(auditEntryEntity.get());
            channelEvent.setAuditEntryId(savedAuditEntryEntity.getId());
            try {
                final Optional<DigestModel> optionalModel = contentConverter.getContent(channelEvent.getContent(), DigestModel.class);
                if (!optionalModel.isPresent()) {
                    return false;
                } else {
                    final Collection<ProjectData> projectDataCollection = optionalModel.get().getProjectDataCollection();
                    projectDataCollection.forEach(projectDataItem -> {
                        projectDataItem.getNotificationIds().forEach(notificationId -> {
                            final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntryEntity.getId(), notificationId);
                            auditNotificationRepository.save(auditNotificationRelation);
                        });
                    });
                    final String jsonMessage = gson.toJson(channelEvent);
                    jmsTemplate.convertAndSend(destination, jsonMessage);
                }
            } catch (final AlertException ex) {
                return false;
            }
        } else {
            final String jsonMessage = gson.toJson(event);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        }
        return true;
    }
}
