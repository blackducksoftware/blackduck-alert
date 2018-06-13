/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.digest.DigestRemovalProcessor;
import com.blackducksoftware.integration.hub.alert.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModels;
import com.google.gson.Gson;

@Component
public class RealTimeListener extends MessageReceiver<AlertEvent> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeListener.class);

    private final ChannelTemplateManager channelTemplateManager;
    private final NotificationEventManager eventManager;
    private final AlertEventContentConverter contentConverter;

    @Autowired
    public RealTimeListener(final Gson gson, final ChannelTemplateManager channelTemplateManager, final NotificationEventManager eventManager, final AlertEventContentConverter contentConverter) {
        super(gson, AlertEvent.class);
        this.channelTemplateManager = channelTemplateManager;
        this.eventManager = eventManager;
        this.contentConverter = contentConverter;

    }

    @Override
    public void handleEvent(final AlertEvent event) {
        try {
            final Optional<NotificationModels> optionalModel = contentConverter.getContent(event.getContent(), NotificationModels.class);
            if (optionalModel.isPresent()) {
                final List<NotificationModel> notificationList = optionalModel.get().getNotificationModelList();
                final DigestRemovalProcessor removalProcessor = new DigestRemovalProcessor();
                final List<NotificationModel> processedNotificationList = removalProcessor.process(notificationList);
                if (!processedNotificationList.isEmpty()) {
                    final List<ChannelEvent> events = eventManager.createChannelEvents(DigestTypeEnum.REAL_TIME, processedNotificationList);
                    channelTemplateManager.sendEvents(events);
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
