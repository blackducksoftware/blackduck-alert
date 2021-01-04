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
package com.synopsys.integration.alert.channel.azure.boards;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsMessageParser;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsRequestCreator;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TestIssueRequestCreator;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.util.UrlUtils;

//TODO: This is a near copy of JiraTestIssueRequestCreator, we should abstract out this class
public class AzureBoardsTestIssueRequestCreator implements TestIssueRequestCreator {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsTestIssueRequestCreator.class);

    private final String customTopic;
    private final String customMessage;
    private final AzureBoardsRequestCreator azureBoardsRequestCreator;
    private final AzureBoardsMessageParser azureBoardsMessageParser;

    public AzureBoardsTestIssueRequestCreator(FieldUtility fieldUtility, AzureBoardsRequestCreator azureBoardsRequestCreator, AzureBoardsMessageParser azureBoardsMessageParser) {
        this(
            azureBoardsRequestCreator,
            azureBoardsMessageParser,
            fieldUtility.getString(TestAction.KEY_CUSTOM_TOPIC).orElse(null),
            fieldUtility.getString(TestAction.KEY_CUSTOM_MESSAGE).orElse(null)
        );
    }

    public AzureBoardsTestIssueRequestCreator(AzureBoardsRequestCreator azureBoardsRequestCreator, AzureBoardsMessageParser azureBoardsMessageParser, @Nullable String customTopic, @Nullable String customMessage) {
        this.customTopic = Optional.ofNullable(customTopic).orElse("Alert Test Message");
        this.customMessage = Optional.ofNullable(customMessage).orElse("Test Message Content");
        this.azureBoardsRequestCreator = azureBoardsRequestCreator;
        this.azureBoardsMessageParser = azureBoardsMessageParser;
    }

    @Override
    public Optional<IssueTrackerRequest> createRequest(IssueOperation operation, String messageId) {
        try {
            ProviderMessageContent providerMessageContent = createTestNotificationContent(ItemOperation.ADD, messageId, customTopic, customMessage);
            ComponentItem arbitraryItem = providerMessageContent.getComponentItems().stream()
                                              .findAny()
                                              .orElseThrow(() -> new AlertException("No actionable component items were found. Cannot create test message content."));

            ContentKey providerContentKey = providerMessageContent.getContentKey();
            String providerName = providerMessageContent.getProvider().getValue();
            String providerUrl = providerMessageContent.getProvider().getUrl()
                                     .map(UrlUtils::formatProviderUrl)
                                     .orElse("");

            LinkableItem topicItem = providerMessageContent.getTopic();
            LinkableItem subTopicItem = providerMessageContent.getSubTopic().orElse(null);
            Set<ComponentItem> componentItems = providerMessageContent.getComponentItems();
            ComponentItem componentItem = componentItems
                                              .stream()
                                              .findFirst()
                                              .orElse(null);
            AzureBoardsSearchProperties azureBoardsSearchProperties = azureBoardsRequestCreator.createIssueSearchProperties(providerName, providerUrl, topicItem, subTopicItem, componentItem, null);

            switch (operation) {
                case RESOLVE:
                    return Optional.of(createResolveIssueRequest(providerContentKey, topicItem, subTopicItem, componentItems, arbitraryItem, azureBoardsSearchProperties));
                case OPEN:
                case UPDATE:
                default:
                    return Optional.of(createCreateOrUpdateIssueRequest(providerContentKey, topicItem, subTopicItem, componentItems, arbitraryItem, azureBoardsSearchProperties));
            }

        } catch (AlertException ex) {
            logger.error("Error create test issue content", ex);
        }

        return Optional.empty();
    }

    // TODO simplify the following 2 methods
    private IssueTrackerRequest createResolveIssueRequest(ContentKey providerContentKey, LinkableItem topicItem, LinkableItem subTopicItem, Set<ComponentItem> componentItems, ComponentItem arbitraryItem,
        IssueSearchProperties issueSearchProperties) {
        IssueContentModel contentModel = azureBoardsMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueResolutionRequest.OPERATION, topicItem, subTopicItem, componentItems, arbitraryItem);
        AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(providerContentKey);
        return IssueResolutionRequest.of(issueSearchProperties, contentModel, alertIssueOrigin);
    }

    private IssueTrackerRequest createCreateOrUpdateIssueRequest(ContentKey providerContentKey, LinkableItem topicItem, LinkableItem subTopicItem, Set<ComponentItem> componentItems, ComponentItem arbitraryItem,
        IssueSearchProperties issueSearchProperties) {
        IssueContentModel contentModel = azureBoardsMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueCreationRequest.OPERATION, topicItem, subTopicItem, componentItems, arbitraryItem);
        AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(providerContentKey);
        return IssueCreationRequest.of(issueSearchProperties, contentModel, alertIssueOrigin);
    }

    private ProviderMessageContent createTestNotificationContent(ItemOperation operation, String messageId, String customTopic, String customMessage) throws AlertException {
        return new ProviderMessageContent.Builder()
                   .applyProvider("Alert", ProviderProperties.UNKNOWN_CONFIG_ID, "Test")
                   .applyTopic("Test Topic", customTopic)
                   .applySubTopic("Test SubTopic", "Test message sent by Alert")
                   .applyComponentItem(createTestComponentItem(operation, messageId, customMessage))
                   .build();
    }

    private ComponentItem createTestComponentItem(ItemOperation operation, String messageId, String customMessage) throws AlertException {
        return new ComponentItem.Builder()
                   .applyOperation(operation)
                   .applyCategory("Test Category")
                   .applyComponentData("Message ID", messageId)
                   .applyCategoryItem("Details", customMessage)
                   .applyNotificationId(1L)
                   .build();
    }

}
