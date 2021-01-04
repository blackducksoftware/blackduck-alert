/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.azure.boards.service;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsSearchProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsSearchPropertiesUtils;
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
