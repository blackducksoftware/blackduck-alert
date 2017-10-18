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
package com.blackducksoftware.integration.hub.alert.batch.digest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class DigestItemProcessor implements ItemProcessor<List<NotificationEntity>, List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);

    @Override
    public List<AbstractChannelEvent> process(final List<NotificationEntity> notificationData) throws Exception {
        logger.info("Notification Entity Count: {}", notificationData.size());

        final List<AbstractChannelEvent> events = processNotifications(notificationData);

        if (events.isEmpty()) {
            return null;
        } else {
            return events;
        }
    }

    private List<AbstractChannelEvent> processNotifications(final List<NotificationEntity> notificationList) {

        if (notificationList == null) {
            return new ArrayList<>(0);
        } else {
            final List<AbstractChannelEvent> events = new ArrayList<>(notificationList.size());
            notificationList.forEach(notification -> {
                events.add(new EmailEvent(notification));
                events.add(new HipChatEvent(notification));
            });
            return events;
        }
    }
}
