/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.alert.common.workflow.processor.message.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

public class BlackDuckMessageContentCollector extends ProviderMessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckMessageContentCollector.class);

    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final Map<String, BlackDuckMessageBuilder> messageBuilderMap;
    private final BlackDuckBucket blackDuckBucket;

    public BlackDuckMessageContentCollector(BlackDuckServicesFactory blackDuckServicesFactory, List<MessageContentProcessor> messageContentProcessors, List<BlackDuckMessageBuilder> messageBuilders) {
        super(messageContentProcessors);
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.messageBuilderMap = DataStructureUtils.mapToValues(messageBuilders, builder -> builder.getNotificationType().name());
        this.blackDuckBucket = new BlackDuckBucket();
    }

    @Override
    protected List<ProviderMessageContent> createProviderMessageContents(DistributionJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) {
        List<ProviderMessageContent> providerMessageContents = new LinkedList<>();
        for (AlertNotificationModel notification : notifications) {
            String notificationType = notification.getNotificationType();
            BlackDuckMessageBuilder blackDuckMessageBuilder = messageBuilderMap.get(notificationType);
            if (null == blackDuckMessageBuilder) {
                logger.warn("Could not find a message builder for notification type: {}", notificationType);
            } else {
                String baseUrlString = blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl().toString();
                CommonMessageData commonMessageData = new CommonMessageData(
                    notification.getId(), notification.getProviderConfigId(), blackDuckMessageBuilder.getProviderName(), notification.getProviderConfigName(), baseUrlString, notification.getProviderCreationTime(), job);
                List<ProviderMessageContent> providerMessageContentsForNotification =
                    blackDuckMessageBuilder.buildMessageContents(commonMessageData, cache.getTypedContent(notification), blackDuckBucket, blackDuckServicesFactory);
                providerMessageContents.addAll(providerMessageContentsForNotification);
            }
        }
        return providerMessageContents;
    }

}
