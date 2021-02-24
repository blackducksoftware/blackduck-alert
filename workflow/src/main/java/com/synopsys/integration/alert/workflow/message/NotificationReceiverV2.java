/*
 * workflow
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
package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertDefaultEventListener;
import com.synopsys.integration.alert.common.event.NotificationReceivedEventV2;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.NotificationProcessorV2;

@Component(value = NotificationReceiverV2.COMPONENT_NAME)
public class NotificationReceiverV2 extends MessageReceiver<NotificationReceivedEventV2> implements AlertDefaultEventListener {
    public static final String COMPONENT_NAME = "notification_receiverV2";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private NotificationAccessor notificationAccessor;
    private NotificationProcessorV2 notificationProcessor;

    @Autowired
    public NotificationReceiverV2(Gson gson, NotificationAccessor notificationAccessor, NotificationProcessorV2 notificationProcessor) {
        super(gson, NotificationReceivedEventV2.class);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
    }

    @Override
    public void handleEvent(NotificationReceivedEventV2 event) {
        logger.debug("Event {}", event);
        logger.info("Processing event for notifications.");

        int numPagesProcessed = 0;
        int pageSize = 100;

        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
        while (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Sending {} notifications.", notifications.size());
            notificationProcessor.processNotifications(notifications, List.of(FrequencyType.REAL_TIME));
            //logger.info("Sending {} events for notifications.", distributionEvents.size());
            //eventManager.sendEvents(distributionEvents);
            //TODO: This may be better moved into the notificationProcessor once the notifications are sent out and processed.
            notificationAccessor.setNotificationsProcessed(notifications);
            numPagesProcessed++;
            pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
            logger.info("Processing Page: {}. New pages found: {}", //TODO set this back to trace level logging
                numPagesProcessed,
                pageOfAlertNotificationModels.getTotalPages());
        }
        logger.info("Finished processing event for notifications.");
    }

    @Override
    public String getDestinationName() {
        return NotificationReceivedEventV2.NOTIFICATION_RECEIVED_EVENT_TYPE;
    }
}
