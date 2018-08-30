/**
 * blackduck-alert
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
package com.synopsys.integration.alert.common.digest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.distribution.CommonDistributionConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
@Transactional
public class NotificationToEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToEventConverter.class);
    private final ChannelEventFactory channelEventFactory;
    private final CommonDistributionConfigReader commonDistributionConfigReader;

    @Autowired
    public NotificationToEventConverter(final ChannelEventFactory channelEventFactory, final CommonDistributionConfigReader commonDistributionConfigReader) {
        this.channelEventFactory = channelEventFactory;
        this.commonDistributionConfigReader = commonDistributionConfigReader;
    }

    public List<ChannelEvent> convertToEvents(final Collection<NotificationContent> sortedNotifications) {
        final List<ChannelEvent> channelEvents = new ArrayList<>();

        final Collection<CommonDistributionConfig> distributionConfigs = commonDistributionConfigReader.getPopulatedConfigs();
        distributionConfigs.forEach(config -> {
            sortedNotifications.forEach(notification -> {
                if (doesNotificationApplyToConfig(config, notification)) {
                    final ChannelEvent newEvent = createChannelEvent(config, notification);
                    channelEvents.add(newEvent);
                }
            });
        });
        logger.debug("Created {} events.", channelEvents.size());
        return channelEvents;
    }

    private boolean doesNotificationApplyToConfig(final CommonDistributionConfig config, final NotificationContent notification) {
        return config.getNotificationTypes().contains(notification.getNotificationType());
    }

    private ChannelEvent createChannelEvent(final CommonDistributionConfig config, final NotificationContent notificationContent) {
        final Long configId = Long.parseLong(config.getId());
        return channelEventFactory.createChannelEvent(configId, config.getDistributionType(), notificationContent);
    }

}
