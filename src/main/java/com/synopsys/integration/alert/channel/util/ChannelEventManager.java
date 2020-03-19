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
package com.synopsys.integration.alert.channel.util;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;

@Component
public class ChannelEventManager extends EventManager {
    private final AuditUtility auditUtility;

    @Autowired
    public ChannelEventManager(final ContentConverter contentConverter, final JmsTemplate jmsTemplate, final AuditUtility auditUtility) {
        super(contentConverter, jmsTemplate);
        this.auditUtility = auditUtility;
    }

    @Override
    @Transactional
    public void sendEvent(final AlertEvent alertEvent) {
        if (alertEvent instanceof DistributionEvent) {
            final String destination = alertEvent.getDestination();
            final DistributionEvent distributionEvent = (DistributionEvent) alertEvent;
            final UUID jobId = UUID.fromString(distributionEvent.getConfigId());
            final Map<Long, Long> notificationIdToAuditId = auditUtility.createAuditEntry(distributionEvent.getNotificationIdToAuditId(), jobId, distributionEvent.getContent());
            distributionEvent.setNotificationIdToAuditId(notificationIdToAuditId);
            final String jsonMessage = getContentConverter().getJsonString(distributionEvent);
            getJmsTemplate().convertAndSend(destination, jsonMessage);
        }
    }

}
