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
package com.synopsys.integration.alert.channel;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;

@Transactional
@Component
public class ChannelTemplateManager {
    private final Gson gson;
    private final JmsTemplate jmsTemplate;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public ChannelTemplateManager(final Gson gson, final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository, final JmsTemplate jmsTemplate) {
        this.gson = gson;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.jmsTemplate = jmsTemplate;
    }

    public void sendEvents(final List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    public boolean sendEvent(final AlertEvent event) {
        final String destination = event.getDestination();
        if (event instanceof ChannelEvent) {
            final ChannelEvent channelEvent = (ChannelEvent) event;
            AuditEntryEntity auditEntryEntity = new AuditEntryEntity(channelEvent.getCommonDistributionConfigId(), new Date(System.currentTimeMillis()), null, null, null, null);

            if (channelEvent.getAuditEntryId() != null) {
                auditEntryEntity = auditEntryRepository.findById(channelEvent.getAuditEntryId()).orElse(auditEntryEntity);
            }

            auditEntryEntity.setStatus(AuditEntryStatus.PENDING);
            final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(auditEntryEntity);
            channelEvent.setAuditEntryId(savedAuditEntryEntity.getId());

            final AggregateMessageContent content = channelEvent.getContent();
            for (final CategoryItem item : content.getCategoryItemList()) {
                final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntryEntity.getId(), item.getNotificationId());
                auditNotificationRepository.save(auditNotificationRelation);
            }
            final String jsonMessage = gson.toJson(channelEvent);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        } else {
            final String jsonMessage = gson.toJson(event);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        }
        return true;
    }
}
