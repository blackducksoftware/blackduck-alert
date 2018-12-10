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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.database.audit.AuditUtility;

@Component
public class ChannelTemplateManager {
    private static final Logger logger = LoggerFactory.getLogger(ChannelTemplateManager.class);
    private final Gson gson;
    private final JmsTemplate jmsTemplate;
    private final AuditUtility auditUtility;

    @Autowired
    public ChannelTemplateManager(final Gson gson, final AuditUtility auditUtility, final JmsTemplate jmsTemplate) {
        this.gson = gson;
        this.auditUtility = auditUtility;
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void sendEvents(final List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    @Transactional
    public boolean sendEvent(final AlertEvent event) {
        final String destination = event.getDestination();
        if (event instanceof DistributionEvent) {
            final DistributionEvent distributionEvent = (DistributionEvent) event;
            logger.error("Sending {} notifications", distributionEvent.getContent().getCategoryItemList().size());

            final Map<Long, Long> notificationIdToAuditId = auditUtility.createAuditEntry(distributionEvent.getNotificationIdToAuditId(), distributionEvent.getCommonDistributionConfigId(), distributionEvent.getContent());
            distributionEvent.setNotificationIdToAuditId(notificationIdToAuditId);
            //TODO delete this logging stream
            notificationIdToAuditId.entrySet().stream().forEach(longLongEntry -> {
                logger.error("Sending event notification {} to audit {} with job {}", longLongEntry.getKey(), longLongEntry.getValue(), distributionEvent.getCommonDistributionConfigId());
            });
            final String jsonMessage = gson.toJson(distributionEvent);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        } else {
            final String jsonMessage = gson.toJson(event);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        }
        return true;
    }
}
