/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

public class BlackDuckMessageContentCollector extends ProviderMessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckMessageContentCollector.class);

    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final Map<String, BlackDuckMessageBuilder> messageBuilderMap;

    public BlackDuckMessageContentCollector(BlackDuckServicesFactory blackDuckServicesFactory, List<MessageContentProcessor> messageContentProcessors, List<BlackDuckMessageBuilder> messageBuilders) {
        super(messageContentProcessors);
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.messageBuilderMap = DataStructureUtils.mapToValues(messageBuilders, builder -> builder.getNotificationType().name());
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
                    blackDuckMessageBuilder.buildMessageContents(commonMessageData, cache.getTypedContent(notification), blackDuckServicesFactory);
                providerMessageContents.addAll(providerMessageContentsForNotification);
            }
        }
        return providerMessageContents;
    }

}
