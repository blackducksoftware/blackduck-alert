/**
 * alert-common
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
package com.synopsys.integration.alert.common.channel.issuetracker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageParser;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageResult;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.exception.IntegrationException;

/**
 * @param <T> A class that represents a single issue.
 */
public abstract class IssueHandler<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IssueTrackerMessageParser issueTrackerMessageParser;

    public IssueHandler(IssueTrackerMessageParser issueTrackerMessageParser) {
        this.issueTrackerMessageParser = issueTrackerMessageParser;
    }

    public final IssueTrackerMessageResult createOrUpdateIssues(IssueConfig issueConfig, MessageContentGroup content) throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        for (ProviderMessageContent messageContent : content.getSubContent()) {
            Set<String> issueKeysForMessage = createOrUpdateIssuesPerComponent(messageContent, issueConfig);
            issueKeys.addAll(issueKeysForMessage);
        }

        String statusMessage = createStatusMessage(issueKeys);
        return new IssueTrackerMessageResult(statusMessage, issueKeys);
    }

    protected Set<String> updateIssueByTopLevelAction(IssueConfig issueConfig, String providerName, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ItemOperation action) throws IntegrationException {
        if (ItemOperation.DELETE == action) {
            List<T> existingIssues = retrieveExistingIssues(issueConfig.getProjectKey(), providerName, providerUrl, topic, nullableSubTopic, null, null);
            logger.debug("Attempting to resolve issues in the project {} for Provider: {}, Provider Project: {}[{}].", issueConfig.getProjectKey(), providerName, topic.getValue(), nullableSubTopic);
            Set<T> updatedIssues = updateExistingIssues(existingIssues, issueConfig, providerName, topic.getName(), action, Set.of());
            return updatedIssues
                       .stream()
                       .map(this::getIssueKey)
                       .collect(Collectors.toSet());
        } else {
            logger.debug("The top level action was not a DELETE action so it will be ignored");
        }
        return Set.of();
    }

    protected Set<String> createOrUpdateIssuesByComponentGroup(IssueConfig issueConfig, String providerName, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, SetMap<String, ComponentItem> groupedComponentItems)
        throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        String projectName = issueConfig.getProjectName();

        SetMap<String, String> missingTransitionToIssues = SetMap.createDefault();
        for (Set<ComponentItem> componentItems : groupedComponentItems.values()) {
            try {
                ComponentItem arbitraryItem = componentItems
                                                  .stream()
                                                  .findAny()
                                                  .orElseThrow(() -> new AlertException(String.format("No actionable component items were found. Provider: %s, Topic: %s, SubTopic: %s", providerName, topic, nullableSubTopic)));
                ItemOperation operation = arbitraryItem.getOperation();
                String trackingKey = createAdditionalTrackingKey(arbitraryItem);

                List<T> existingIssues = retrieveExistingIssues(issueConfig.getProjectKey(), providerName, providerUrl, topic, nullableSubTopic, arbitraryItem, trackingKey);
                logIssueAction(operation, projectName, providerUrl, providerName, topic, nullableSubTopic, arbitraryItem);
                if (!existingIssues.isEmpty()) {
                    Set<T> updatedIssues = updateExistingIssues(existingIssues, issueConfig, providerName, arbitraryItem.getCategory(), arbitraryItem.getOperation(), componentItems);
                    updatedIssues
                        .stream()
                        .map(this::getIssueKey)
                        .forEach(issueKeys::add);
                } else if (ItemOperation.ADD == operation || ItemOperation.UPDATE == operation) {
                    IssueContentModel contentModel = issueTrackerMessageParser.createIssueContentModel(providerName, topic, nullableSubTopic, componentItems, arbitraryItem);
                    T issueModel = createIssue(issueConfig, providerName, providerUrl, topic, nullableSubTopic, arbitraryItem, trackingKey, contentModel);
                    String issueKey = getIssueKey(issueModel);
                    issueKeys.add(issueKey);
                } else {
                    logger.warn("Expected to find an existing issue, but none existed.");
                }
            } catch (IssueMissingTransitionException e) {
                missingTransitionToIssues.add(e.getTransition(), e.getIssueKey());
            }
        }
        if (!missingTransitionToIssues.isEmpty()) {
            StringBuilder missingTransitions = new StringBuilder();
            for (Map.Entry<String, Set<String>> entry : missingTransitionToIssues.entrySet()) {
                String issues = StringUtils.join(entry.getValue(), ", ");
                missingTransitions.append(String.format("Unable to find the transition: %s, for the issue(s): %s", entry.getKey(), issues));
            }

            String errorMessage = String.format("For Provider: %s. Project: %s. %s.", providerName, projectName, missingTransitions.toString());
            throw new AlertException(errorMessage);
        }
        return issueKeys;
    }

    protected abstract T createIssue(IssueConfig issueConfig, String providerName, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem arbitraryItem,
        String trackingKey, IssueContentModel contentModel) throws IntegrationException;

    protected abstract List<T> retrieveExistingIssues(String projectSearchIdentifier, String provider, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem componentItem, String alertIssueUniqueId)
        throws IntegrationException;

    protected abstract boolean transitionIssue(T issueModel, IssueConfig issueConfig, ItemOperation operation) throws IntegrationException;

    protected abstract void addComment(String issueKey, String comment) throws IntegrationException;

    protected abstract String createAdditionalTrackingKey(ComponentItem componentItem);

    protected abstract String getIssueKey(T issueModel);

    protected abstract String getIssueTrackerUrl();

    protected Set<T> updateExistingIssues(List<T> issuesToUpdate, IssueConfig issueConfig, String providerName, String category, ItemOperation operation, Set<ComponentItem> componentItems)
        throws IntegrationException {
        Set<T> updatedIssues = new HashSet<>();
        for (T issue : issuesToUpdate) {
            String issueKey = getIssueKey(issue);
            if (issueConfig.getCommentOnIssues()) {
                List<String> operationComments = issueTrackerMessageParser.createOperationComment(providerName, category, operation, componentItems);
                for (String operationComment : operationComments) {
                    addComment(issueKey, operationComment);
                }
                updatedIssues.add(issue);
            }

            boolean didUpdateIssue = transitionIssue(issue, issueConfig, operation);
            if (didUpdateIssue) {
                updatedIssues.add(issue);
            }
        }
        return updatedIssues;
    }

    private Set<String> createOrUpdateIssuesPerComponent(ProviderMessageContent messageContent, IssueConfig issueConfig) throws IntegrationException {
        String providerName = messageContent.getProvider().getValue();
        LinkableItem topic = messageContent.getTopic();
        LinkableItem nullableSubTopic = messageContent.getSubTopic().orElse(null);

        String providerUrl = messageContent.getProvider().getUrl()
                                 .map(this::formatUrl)
                                 .orElse("");

        Set<String> issueKeys;
        if (messageContent.isTopLevelActionOnly()) {
            issueKeys = updateIssueByTopLevelAction(issueConfig, providerName, providerUrl, topic, nullableSubTopic, messageContent.getAction().orElse(ItemOperation.INFO));
        } else {
            issueKeys = createOrUpdateIssuesByComponentGroup(issueConfig, providerName, providerUrl, topic, nullableSubTopic, messageContent.groupRelatedComponentItems());
        }
        return issueKeys;
    }

    private String formatUrl(String originalUrl) {
        String correctedUrl = "";
        if (StringUtils.isNotBlank(originalUrl)) {
            correctedUrl = originalUrl.trim();
            if (!correctedUrl.endsWith("/")) {
                correctedUrl += "/";
            }
        }
        return correctedUrl;
    }

    private String createStatusMessage(Collection<String> issueKeys) {
        if (issueKeys.isEmpty()) {
            return "Did not create any issues.";
        }
        String concatenatedKeys = String.join(", ", issueKeys);
        logger.debug("Issues updated: {}", concatenatedKeys);
        return String.format("Successfully created issue at %s. Issue Keys: (%s)", getIssueTrackerUrl(), concatenatedKeys);
    }

    private void logIssueAction(ItemOperation operation, String issueTrackerProjectName, String providerName, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem arbitraryItem) {
        String issueTrackerProjectVersion = nullableSubTopic != null ? nullableSubTopic.getValue() : "unknown";
        String arbitraryItemSubComponent = arbitraryItem.getSubComponent().map(LinkableItem::getValue).orElse("unknown");
        logger.debug("Attempting the {} action on the project {}. Provider: {}, Provider Url:{},  Provider Project: {}[{}]. Category: {}, Component: {}, SubComponent: {}.",
            operation.name(), issueTrackerProjectName, providerName, providerUrl, topic.getValue(), issueTrackerProjectVersion, arbitraryItem.getCategory(), arbitraryItem.getComponent().getName(), arbitraryItemSubComponent);
    }

}
