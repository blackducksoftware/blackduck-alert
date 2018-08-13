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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;

@Transactional
@Component
public class NotificationEventManager {
    private final Logger logger = LoggerFactory.getLogger(NotificationEventManager.class);
    private final NotificationPostProcessor notificationPostProcessor;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ChannelEventFactory channelEventFactory;

    @Autowired
    public NotificationEventManager(final NotificationPostProcessor notificationPostProcessor, final ChannelEventFactory channelEventFactory,
            final CommonDistributionRepository commonDistributionRepository) {
        this.notificationPostProcessor = notificationPostProcessor;
        this.channelEventFactory = channelEventFactory;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public List<ChannelEvent> createChannelEvents(final DigestType digestType, final List<NotificationContent> notificationContentList) {
        final List<ChannelEvent> channelEvents = new ArrayList<>();
        final List<CommonDistributionConfigEntity> distributionConfigurations = commonDistributionRepository.findAll();
        final Map<CommonDistributionConfigEntity, List<NotificationContent>> distributionConfigNotificationMap = new HashMap<>(distributionConfigurations.size());

        distributionConfigurations.forEach(distributionConfig -> {
            distributionConfigNotificationMap.put(distributionConfig, new ArrayList<>());
        });

        notificationContentList.forEach(notificationContent -> {
            final Set<CommonDistributionConfigEntity> applicableConfigurations = notificationPostProcessor.getApplicableConfigurations(distributionConfigurations, notificationContent, digestType);

            applicableConfigurations.forEach(distributionConfig -> {
                final Optional<NotificationContent> filteredNotification = notificationPostProcessor.filterMatchingNotificationTypes(distributionConfig, notificationContent);
                filteredNotification.ifPresent(notification -> {
                    distributionConfigNotificationMap.get(distributionConfig).add(notification);
                });
            });
        });

        distributionConfigNotificationMap.entrySet().forEach(entry -> {
            final CommonDistributionConfigEntity distributionConfig = entry.getKey();
            final List<NotificationContent> notificationList = entry.getValue();
            if (!notificationList.isEmpty()) {
                notificationList.forEach(notificationContent -> {
                    channelEvents.add(createChannelEvent(distributionConfig, notificationContent));
                });
            }
        });
        logger.debug("Created {} events.", channelEvents.size());
        return channelEvents;
    }

    private ChannelEvent createChannelEvent(final CommonDistributionConfigEntity commonEntity, final NotificationContent notificationContent) {
        return channelEventFactory.createChannelEvent(commonEntity.getId(), commonEntity.getDistributionType(), notificationContent);
    }

}
