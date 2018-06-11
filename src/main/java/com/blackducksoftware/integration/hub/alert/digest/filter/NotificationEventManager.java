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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.ArrayList;
import java.util.Collection;
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

import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelEventFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Transactional
@Component
public class NotificationEventManager {
    private final Logger logger = LoggerFactory.getLogger(NotificationEventManager.class);
    private final NotificationPostProcessor notificationPostProcessor;
    private final CommonDistributionRepository commonDistributionRepository;
    private final ChannelEventFactory<DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory;
    private final ProjectDataFactory projectDataFactory;

    @Autowired
    public NotificationEventManager(final NotificationPostProcessor notificationPostProcessor, final ChannelEventFactory<DistributionChannelConfigEntity, GlobalChannelConfigEntity, CommonDistributionConfigRestModel> channelEventFactory,
            final CommonDistributionRepository commonDistributionRepository, final ProjectDataFactory projectDataFactory) {
        this.notificationPostProcessor = notificationPostProcessor;
        this.channelEventFactory = channelEventFactory;
        this.commonDistributionRepository = commonDistributionRepository;
        this.projectDataFactory = projectDataFactory;
    }

    public List<ChannelEvent> createChannelEvents(final DigestTypeEnum digestType, final List<NotificationModel> notificationModelList) {
        final List<ChannelEvent> channelEvents = new ArrayList<>();
        final List<CommonDistributionConfigEntity> distributionConfigurations = commonDistributionRepository.findAll();
        final Map<CommonDistributionConfigEntity, List<NotificationModel>> distributionConfigNotificationMap = new HashMap<>(distributionConfigurations.size());

        distributionConfigurations.forEach(distributionConfig -> {
            distributionConfigNotificationMap.put(distributionConfig, new ArrayList<>());
        });

        notificationModelList.forEach(notificationModel -> {
            final Set<CommonDistributionConfigEntity> applicableConfigurations = notificationPostProcessor.getApplicableConfigurations(distributionConfigurations, notificationModel, digestType);

            applicableConfigurations.forEach(distributionConfig -> {
                final Optional<NotificationModel> filteredNotification = notificationPostProcessor.filterMatchingNotificationTypes(distributionConfig, notificationModel);
                filteredNotification.ifPresent(notification -> {
                    distributionConfigNotificationMap.get(distributionConfig).add(notification);
                });
            });
        });

        distributionConfigNotificationMap.entrySet().forEach(entry -> {
            final CommonDistributionConfigEntity distributionConfig = entry.getKey();
            final List<NotificationModel> notificationList = entry.getValue();
            if (!notificationList.isEmpty()) {
                final Collection<ProjectData> projectData = projectDataFactory.createProjectDataCollection(notificationList, digestType);
                channelEvents.add(createChannelEvent(distributionConfig, new DigestModel(projectData)));
            }
        });
        logger.debug("Created {} events.", channelEvents.size());
        return channelEvents;
    }

    private ChannelEvent createChannelEvent(final CommonDistributionConfigEntity commonEntity, final DigestModel digestModel) {
        return channelEventFactory.createEvent(commonEntity.getId(), commonEntity.getDistributionType(), digestModel);
    }

}
