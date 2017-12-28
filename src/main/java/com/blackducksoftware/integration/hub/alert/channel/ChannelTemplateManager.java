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
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.alert.datasource.entity.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.AuditNotificationRepository;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.event.AbstractEvent;
import com.google.gson.Gson;

@Component
public class ChannelTemplateManager {
    private final Map<String, AbstractJmsTemplate> jmsTemplateMap;
    private final Gson gson;
    private final List<AbstractJmsTemplate> templateList;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public ChannelTemplateManager(final Gson gson, final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository, final List<AbstractJmsTemplate> templateList) {
        jmsTemplateMap = new HashMap<>();
        this.gson = gson;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.templateList = templateList;
    }

    @PostConstruct
    public void init() {
        templateList.forEach(jmsTemplate -> {
            addTemplate(jmsTemplate.getDestinationName(), jmsTemplate);
        });
    }

    public boolean hasTemplate(final String destination) {
        return jmsTemplateMap.containsKey(destination);
    }

    public AbstractJmsTemplate getTemplate(final String destination) {
        return jmsTemplateMap.get(destination);
    }

    public void addTemplate(final String destination, final AbstractJmsTemplate template) {
        jmsTemplateMap.put(destination, template);
    }

    public void sendEvents(final List<? extends AbstractEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(event -> {
                sendEvent(event);

            });
        }
    }

    public boolean sendEvent(final AbstractEvent event) {
        final String destination = event.getTopic();
        if (hasTemplate(destination)) {
            if (event instanceof AbstractChannelEvent) {
                final AbstractChannelEvent channelEvent = (AbstractChannelEvent) event;
                final List<String> ids = channelEvent.getProjectData().getNotificationIds().stream().map(id -> id.toString()).collect(Collectors.toList());
                // TODO remove println
                System.out.println("Notification Id's : " + StringUtils.join(ids));
                // TODO update AuditEntryEntity to handle multiple notifications
                final AuditEntryEntity auditEntryEntity = new AuditEntryEntity(channelEvent.getCommonDistributionConfigId(), new Date(), null, null, null, null);
                final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(auditEntryEntity);
                // FIXME WHY IS THE ID NOT BEING GENERATED??
                channelEvent.setAuditEntryId(savedAuditEntryEntity.getId());
                System.out.println("Audit " + auditEntryEntity);
                System.out.println("Saved Audit " + savedAuditEntryEntity);
                // TODO remove println
                System.out.println("Audit Entity Id : " + channelEvent.getAuditEntryId());

                for (final Long notificationId : channelEvent.getProjectData().getNotificationIds()) {
                    final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntryEntity.getId(), notificationId);
                    auditNotificationRepository.save(auditNotificationRelation);
                }
                final String jsonMessage = gson.toJson(channelEvent);
                final AbstractJmsTemplate template = getTemplate(destination);
                template.convertAndSend(destination, jsonMessage);
            } else {
                final String jsonMessage = gson.toJson(event);
                final AbstractJmsTemplate template = getTemplate(destination);
                template.convertAndSend(destination, jsonMessage);
            }
            return true;

        } else {
            return false;
        }
    }
}
