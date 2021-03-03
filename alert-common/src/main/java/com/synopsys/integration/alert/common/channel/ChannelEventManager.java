/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

@Component
public class ChannelEventManager extends EventManager {
    private final AuditAccessor auditAccessor;

    @Autowired
    public ChannelEventManager(ContentConverter contentConverter, JmsTemplate jmsTemplate, AuditAccessor auditAccessor) {
        super(contentConverter, jmsTemplate);
        this.auditAccessor = auditAccessor;
    }

    @Override
    @Transactional
    public void sendEvent(AlertEvent alertEvent) {
        if (alertEvent instanceof DistributionEvent) {
            String destination = alertEvent.getDestination();
            DistributionEvent distributionEvent = (DistributionEvent) alertEvent;
            DistributionJobModel distributionJobModel = distributionEvent.getDistributionJobModel();

            UUID jobId = distributionJobModel.getJobId();
            Map<Long, Long> notificationIdToAuditId = auditAccessor.createAuditEntry(distributionEvent.getNotificationIdToAuditId(), jobId, distributionEvent.getContent());
            distributionEvent.setNotificationIdToAuditId(notificationIdToAuditId);
            String jsonMessage = getContentConverter().getJsonString(distributionEvent);
            getJmsTemplate().convertAndSend(destination, jsonMessage);
        }
    }

}
