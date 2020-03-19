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
package com.synopsys.integration.alert.workflow.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.util.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;

@Component(value = NotificationReceiver.COMPONENT_NAME)
public class NotificationReceiver extends MessageReceiver<NotificationEvent> implements AlertEventListener {
    public static final String COMPONENT_NAME = "notification_receiver";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationManager notificationManager;
    private final NotificationProcessor notificationProcessor;
    private final ChannelEventManager eventManager;

    @Autowired
    public NotificationReceiver(final Gson gson, final NotificationManager notificationManager, final NotificationProcessor notificationProcessor, final ChannelEventManager eventManager) {
        super(gson, NotificationEvent.class);
        this.notificationManager = notificationManager;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(final NotificationEvent event) {
        if (NotificationEvent.NOTIFICATION_EVENT_TYPE.equals(event.getDestination())) {
            if (null == event.getNotificationIds() || event.getNotificationIds().isEmpty()) {
                logger.warn("Can not process a notification event without notification Id's.");
                return;
            }
            logger.info("Processing event for {} notifications.", event.getNotificationIds().size());
            final List<AlertNotificationWrapper> notifications = notificationManager.findByIds(event.getNotificationIds());
            final List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(FrequencyType.REAL_TIME, notifications);
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
