/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.service;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsSearchProperties;
import com.synopsys.integration.alert.channel.azure.boards2.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueTrackerRequestCreator;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class AzureBoardsRequestCreator extends IssueTrackerRequestCreator {
    public AzureBoardsRequestCreator(AzureBoardsMessageParser messageParser) {
        super(messageParser);
    }

    @Override
    public AzureBoardsSearchProperties createIssueSearchProperties(String providerName, String providerUrl, LinkableItem topic, @Nullable LinkableItem subTopic, @Nullable ComponentItem componentItem, @Nullable String additionalInfo) {
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(providerName, providerUrl);
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(topic);
        String subTopicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(subTopic);

        String categoryKey = null;
        String componentKey = null;
        String subComponentKey = null;
        if (null != componentItem) {
            categoryKey = componentItem.getCategory();
            componentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(componentItem.getComponent());
            subComponentKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(componentItem.getSubComponent().orElse(null));
        }
        return new AzureBoardsSearchProperties(providerKey, topicKey, subTopicKey, categoryKey, componentKey, subComponentKey, additionalInfo);
    }

}
