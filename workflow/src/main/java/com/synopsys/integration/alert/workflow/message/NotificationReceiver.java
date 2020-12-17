/**
 * workflow
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
package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;

@Component(value = NotificationReceiver.COMPONENT_NAME)
public class NotificationReceiver extends MessageReceiver<NotificationReceivedEvent> implements AlertEventListener {
    private final static int MAX_NUMBER_PAGES_PROCESSED = 100;
    public static final String COMPONENT_NAME = "notification_receiver";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private NotificationAccessor notificationAccessor;
    private NotificationProcessor notificationProcessor;
    private ChannelEventManager eventManager;

    @Autowired
    public NotificationReceiver(Gson gson, NotificationAccessor notificationAccessor, NotificationProcessor notificationProcessor, ChannelEventManager eventManager) {
        super(gson, NotificationReceivedEvent.class);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(NotificationReceivedEvent event) {
        logger.debug("Event {}", event);
        logger.info("Processing event for notifications.");

        int numPagesProcessed = 0;
        int pageSize = 100;

        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
        //TODO: Once we create a way of handling channel events in parallel, we can remove the MAX_NUMBER_PAGES_PROCESSED.
        while (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels()) && numPagesProcessed < MAX_NUMBER_PAGES_PROCESSED) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Sending {} notifications.", notifications.size());
            List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(FrequencyType.REAL_TIME, notifications);
            logger.info("Sending {} events for notifications.", distributionEvents.size());
            eventManager.sendEvents(distributionEvents);
            notificationAccessor.setNotificationsProcessed(notifications);
            numPagesProcessed++;
            pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
            logger.trace("Processing Page: {}. New pages found: {}",
                numPagesProcessed,
                pageOfAlertNotificationModels.getTotalPages());
        }
        if (numPagesProcessed == MAX_NUMBER_PAGES_PROCESSED) {
            logger.warn("Receiver reached upper page limit of pages processed: {}, exiting.", MAX_NUMBER_PAGES_PROCESSED);
        }
    }

    @Override
    public String getDestinationName() {
        return NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE;
    }
}
