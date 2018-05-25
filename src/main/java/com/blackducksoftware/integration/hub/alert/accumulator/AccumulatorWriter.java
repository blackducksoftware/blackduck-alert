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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.event.RealTimeEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

@Transactional
public class AccumulatorWriter implements ItemWriter<DBStoreEvent> {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorWriter.class);
    private final NotificationManager notificationManager;
    private final ChannelTemplateManager channelTemplateManager;

    public AccumulatorWriter(final NotificationManager notificationManager, final ChannelTemplateManager channelTemplateManager) {
        this.notificationManager = notificationManager;
        this.channelTemplateManager = channelTemplateManager;
    }

    @Override
    public void write(final List<? extends DBStoreEvent> itemList) throws Exception {
        try {
            if (itemList != null && !itemList.isEmpty()) {
                logger.info("Writing {} notifications", itemList.size());
                itemList.forEach(item -> {
                    final List<NotificationModel> notificationList = item.getNotificationList();
                    final List<NotificationModel> entityList = new ArrayList<>();
                    notificationList.forEach(notification -> {
                        notificationManager.saveNotification(notification);
                        entityList.add(notification);
                    });
                    final RealTimeEvent realTimeEvent = new RealTimeEvent(entityList);
                    channelTemplateManager.sendEvent(realTimeEvent);
                });
            } else {
                logger.info("No notifications to write");
            }
        } catch (final Exception ex) {
            logger.error("Error occurred writing notification data", ex);
        }
    }
}
