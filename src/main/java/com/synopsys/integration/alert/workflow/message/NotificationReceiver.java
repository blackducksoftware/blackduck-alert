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
package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;

@Component(value = NotificationReceiver.COMPONENT_NAME)
public class NotificationReceiver extends MessageReceiver<NotificationEvent> implements AlertEventListener {
    public static final String COMPONENT_NAME = "notification_receiver";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private NotificationManager notificationManager;
    private NotificationProcessor notificationProcessor;
    private ChannelEventManager eventManager;

    @Autowired
    public NotificationReceiver(Gson gson, NotificationManager notificationManager, NotificationProcessor notificationProcessor, ChannelEventManager eventManager) {
        super(gson, NotificationEvent.class);
        this.notificationManager = notificationManager;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(NotificationEvent event) {
        if (NotificationEvent.NOTIFICATION_EVENT_TYPE.equals(event.getDestination())) {
            if (null == event.getNotificationIds() || event.getNotificationIds().isEmpty()) {
                logger.warn("Can not process a notification event without notification Id's.");
                return;
            }
            logger.debug("Event {}", event);
            logger.info("Processing event for {} notifications.", event.getNotificationIds().size());
            List<AlertNotificationModel> notifications = notificationManager.findByIds(event.getNotificationIds());
            List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(FrequencyType.REAL_TIME, notifications);
            logger.info("Sending {} events for notifications.", distributionEvents.size());
            eventManager.sendEvents(distributionEvents);
        } else {
            logger.warn("Received an event of type '{}', but this listener is for type '{}'.", event.getDestination(), NotificationEvent.NOTIFICATION_EVENT_TYPE);
        }
    }

    @Override
    public String getDestinationName() {
        return NotificationEvent.NOTIFICATION_EVENT_TYPE;
    }
}
