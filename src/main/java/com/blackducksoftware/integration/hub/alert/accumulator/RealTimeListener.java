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
package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.digest.DigestNotificationProcessor;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.event.RealTimeEvent;
import com.google.gson.Gson;

@Component
public class RealTimeListener extends MessageReceiver<RealTimeEvent> {
    private final ChannelTemplateManager channelTemplateManager;
    private final DigestNotificationProcessor notificationProcessor;

    @Autowired
    public RealTimeListener(final Gson gson, final ChannelTemplateManager channelTemplateManager, final DigestNotificationProcessor notificationProcessor) {
        super(gson, RealTimeEvent.class);
        this.channelTemplateManager = channelTemplateManager;
        this.notificationProcessor = notificationProcessor;
    }

    @JmsListener(destination = RealTimeEvent.TOPIC_NAME)
    @Override
    public void receiveMessage(final String message) {
        final RealTimeEvent event = getEvent(message);
        final List<AbstractChannelEvent> events = notificationProcessor.processNotifications(DigestTypeEnum.REAL_TIME, event.getNotificationList());
        channelTemplateManager.sendEvents(events);
    }
}
