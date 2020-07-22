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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.common.util.JiraIssuePropertiesUtil;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCommentRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.datastructure.SetMap;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class JiraMessageContentConverter {
    private final Logger logger = LoggerFactory.getLogger(JiraMessageContentConverter.class);
    private final JiraMessageParser jiraMessageParser;

    @Autowired
    public JiraMessageContentConverter(JiraMessageParser jiraMessageParser) {
        this.jiraMessageParser = jiraMessageParser;
    }

    public List<IssueTrackerRequest> convertMessageContents(IssueConfig issueConfig, MessageContentGroup content) throws IntegrationException {
        List<IssueTrackerRequest> issues = new LinkedList<>();
        for (ProviderMessageContent messageContent : content.getSubContent()) {
            List<IssueTrackerRequest> issueKeysForMessage = createOrUpdateIssuesPerComponent(issueConfig, messageContent);
            issues.addAll(issueKeysForMessage);
        }
        return issues;
    }

    protected List<IssueTrackerRequest> updateIssueByTopLevelAction(IssueConfig issueConfig, ContentKey providerContentKey, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ItemOperation action) {
        if (ItemOperation.DELETE == action) {
            logger.debug("Attempting to resolve issues in the project {} for Provider: {}, Provider Project: {}[{}].", issueConfig.getProjectKey(), providerContentKey.getProviderName(), topic.getValue(), nullableSubTopic);
            String trackingKey = createAdditionalTrackingKey(null);
            IssueSearchProperties issueSearchProperties = JiraIssuePropertiesUtil.create(providerContentKey.getProviderName(), providerUrl, topic, nullableSubTopic, null, trackingKey);
            IssueContentModel issueContentModel = jiraMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueResolutionRequest.OPERATION, topic, nullableSubTopic, Set.of(), null);
            AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(providerContentKey, null);
            IssueTrackerRequest issueRequest = IssueResolutionRequest.of(issueSearchProperties, issueContentModel, alertIssueOrigin);
            return List.of(issueRequest);
        } else {
            logger.debug("The top level action was not a DELETE action so it will be ignored");
        }
        return List.of();
    }

    protected List<IssueTrackerRequest> createOrUpdateIssuesByComponentGroup(ContentKey providerContentKey, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic,
        SetMap<String, ComponentItem> groupedComponentItems) throws IntegrationException {
        List<IssueTrackerRequest> issues = new LinkedList<>();

        for (Set<ComponentItem> componentItems : groupedComponentItems.values()) {
            ComponentItem arbitraryItem = componentItems
                                              .stream()
                                              .findAny()
                                              .orElseThrow(
                                                  () -> new AlertException(String.format("No actionable component items were found. Provider: %s, Topic: %s, SubTopic: %s", providerContentKey.getProviderName(), topic, nullableSubTopic)));
            ItemOperation operation = arbitraryItem.getOperation();
            String trackingKey = createAdditionalTrackingKey(arbitraryItem);
            IssueSearchProperties issueSearchProperties = JiraIssuePropertiesUtil.create(providerContentKey.getProviderName(), providerUrl, topic, nullableSubTopic, arbitraryItem, trackingKey);

            IssueTrackerRequest issueRequest = null;
            AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(providerContentKey, arbitraryItem);
            if (ItemOperation.ADD == operation || ItemOperation.UPDATE == operation) {
                IssueContentModel issueContentModel = jiraMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueCreationRequest.OPERATION, topic, nullableSubTopic, componentItems, arbitraryItem);
                issueRequest = IssueCreationRequest.of(issueSearchProperties, issueContentModel, alertIssueOrigin);
            } else if (ItemOperation.DELETE == operation) {
                IssueContentModel issueContentModel = jiraMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueResolutionRequest.OPERATION, topic, nullableSubTopic, componentItems, arbitraryItem);
                issueRequest = IssueResolutionRequest.of(issueSearchProperties, issueContentModel, alertIssueOrigin);
            } else if (ItemOperation.INFO == operation) {
                IssueContentModel issueContentModel = jiraMessageParser.createIssueContentModel(providerContentKey.getProviderName(), IssueCommentRequest.OPERATION, topic, nullableSubTopic, componentItems, arbitraryItem);
                issueRequest = IssueCommentRequest.of(issueSearchProperties, issueContentModel, alertIssueOrigin);
            }
            if (null != issueRequest) {
                issues.add(issueRequest);
            }
        }
        return issues;
    }

    protected String createAdditionalTrackingKey(ComponentItem componentItem) {
        if (null != componentItem && !componentItem.collapseOnCategory()) {
            LinkableItem categoryItem = componentItem.getCategoryItem();
            return categoryItem.getName() + categoryItem.getValue();
        }
        return StringUtils.EMPTY;
    }

    private List<IssueTrackerRequest> createOrUpdateIssuesPerComponent(IssueConfig issueConfig, ProviderMessageContent messageContent) throws IntegrationException {
        LinkableItem topic = messageContent.getTopic();
        LinkableItem nullableSubTopic = messageContent.getSubTopic().orElse(null);

        ContentKey providerContentKey = messageContent.getContentKey();
        String providerUrl = messageContent.getProvider().getUrl()
                                 .map(JiraIssuePropertiesUtil::formatProviderUrl)
                                 .orElse("");
        List<IssueTrackerRequest> requests;
        if (messageContent.isTopLevelActionOnly()) {
            requests = updateIssueByTopLevelAction(issueConfig, providerContentKey, providerUrl, topic, nullableSubTopic, messageContent.getAction().orElse(ItemOperation.INFO));
        } else {
            requests = createOrUpdateIssuesByComponentGroup(providerContentKey, providerUrl, topic, nullableSubTopic, messageContent.groupRelatedComponentItems());
        }
        return requests;
    }

}
