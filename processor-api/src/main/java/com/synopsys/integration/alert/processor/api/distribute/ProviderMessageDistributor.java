/**
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.model.NotificationFilterJobModel;

@Component
public class ProviderMessageDistributor {
    private static final String EVENT_CLASS_NAME = DistributionEventV2.class.getSimpleName();
    private static final String DESTINATION_WRAPPER_CLASS_NAME = ChannelKey.class.getSimpleName();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuditAccessor auditAccessor;
    private final EventManager eventManager;

    @Autowired
    public ProviderMessageDistributor(AuditAccessor auditAccessor, EventManager eventManager) {
        this.auditAccessor = auditAccessor;
        this.eventManager = eventManager;
    }

    public void distribute(NotificationFilterJobModel notificationFilterJobModel, ProviderMessageHolder providerMessageHolder) {
        UUID jobId = notificationFilterJobModel.getJobId();
        Long auditId = auditAccessor.createAuditEntryForJob(jobId, notificationFilterJobModel.getNotificationIds());

        String channelName = notificationFilterJobModel.getChannelName();
        ChannelKey destinationKey = ChannelKeys.getChannelKey(channelName);
        if (null != destinationKey) {
            DistributionEventV2 event = new DistributionEventV2(destinationKey, jobId, auditId, providerMessageHolder);
            logger.info("Sending {}. Event ID: {}. Job ID: {}. Destination: {}", EVENT_CLASS_NAME, event.getEventId(), jobId, destinationKey);
            eventManager.sendEvent(event);
        } else {
            logger.warn("Unable to send {}. No {} with the name {} exists", EVENT_CLASS_NAME, DESTINATION_WRAPPER_CLASS_NAME, channelName);
        }
    }

}
