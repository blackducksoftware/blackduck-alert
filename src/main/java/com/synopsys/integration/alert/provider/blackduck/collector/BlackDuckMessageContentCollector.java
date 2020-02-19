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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.formatter.MessageContentFormatter;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckMessageBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BlackDuckMessageContentCollector extends ProviderMessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckMessageContentCollector.class);

    private BlackDuckProperties blackDuckProperties;
    private Map<String, BlackDuckMessageBuilder> messageBuilderMap;
    private BlackDuckBucket blackDuckBucket;

    @Autowired
    public BlackDuckMessageContentCollector(BlackDuckProperties blackDuckProperties, List<MessageContentFormatter> messageContentProcessors, List<BlackDuckMessageBuilder> messageBuilders) {
        super(messageContentProcessors);
        this.blackDuckProperties = blackDuckProperties;
        this.messageBuilderMap = DataStructureUtils.mapToValues(messageBuilders, BlackDuckMessageBuilder::getNotificationType);
        this.blackDuckBucket = new BlackDuckBucket();
    }

    @Override
    protected List<ProviderMessageContent> createProviderMessageContents(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory();

        List<ProviderMessageContent> providerMessageContents = new LinkedList<>();
        for (AlertNotificationModel notification : notifications) {
            String notificationType = notification.getNotificationType();
            BlackDuckMessageBuilder blackDuckMessageBuilder = messageBuilderMap.get(notificationType);
            if (null == blackDuckMessageBuilder) {
                logger.warn("Could not find a message builder for notification type: {}", notificationType);
            } else {
                String url = blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl();
                CommonMessageData commonMessageData = new CommonMessageData(notification.getId(), notification.getProviderConfigId(), notification.getProviderConfigName(), url, notification.getProviderCreationTime(), job);
                List<ProviderMessageContent> providerMessageContentsForNotification =
                    blackDuckMessageBuilder
                        .buildMessageContents(commonMessageData, cache.getTypedContent(notification), blackDuckBucket, blackDuckServicesFactory);
                providerMessageContents.addAll(providerMessageContentsForNotification);
            }
        }
        return providerMessageContents;
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory() throws AlertException {
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
            return blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, blackDuckHttpClient.getLogger());
        }
        throw new AlertException("Cannot create message content with no connection to Black Duck");
    }

}
