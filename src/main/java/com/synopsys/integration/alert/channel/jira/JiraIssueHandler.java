/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.jira.JiraIssueConfigValidator.JiraIssueConfig;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.exception.JiraMissingTransitionException;
import com.synopsys.integration.alert.channel.jira.model.JiraMessageResult;
import com.synopsys.integration.alert.channel.jira.util.JiraIssueFormatHelper;
import com.synopsys.integration.alert.channel.jira.util.JiraIssuePropertyHelper;
import com.synopsys.integration.alert.channel.jira.util.JiraTransitionHelper;
import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.model.components.IssueComponent;
import com.synopsys.integration.jira.common.cloud.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueService;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class JiraIssueHandler {
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueHandler.class);

    private final IssueService issueService;
    private final JiraProperties jiraProperties;
    private final Gson gson;

    private final JiraTransitionHelper jiraTransitionHelper;
    private final JiraIssuePropertyHelper jiraIssuePropertyHelper;

    public JiraIssueHandler(IssueService issueService, IssueSearchService issueSearchService, IssuePropertyService issuePropertyService, JiraProperties jiraProperties, Gson gson) {
        this.issueService = issueService;
        this.jiraProperties = jiraProperties;
        this.gson = gson;
        this.jiraTransitionHelper = new JiraTransitionHelper(issueService);
        this.jiraIssuePropertyHelper = new JiraIssuePropertyHelper(issueSearchService, issuePropertyService);
    }

    public JiraMessageResult createOrUpdateIssues(JiraIssueConfig jiraIssueConfig, MessageContentGroup content) throws IntegrationException {
        String providerName = content.getComonProvider().getValue();
        LinkableItem commonTopic = content.getCommonTopic();

        Set<String> issueKeys = new HashSet<>();
        for (ProviderMessageContent messageContent : content.getSubContent()) {
            Optional<LinkableItem> subTopic = messageContent.getSubTopic();

            Set<String> issueKeysForMessage = createOrUpdateIssuesPerComponent(providerName, commonTopic, subTopic, messageContent.getComponentItems(), jiraIssueConfig);
            issueKeys.addAll(issueKeysForMessage);
        }

        String statusMessage = createStatusMessage(issueKeys, jiraProperties.getUrl());
        return new JiraMessageResult(statusMessage, issueKeys);
    }

    private Set<String> createOrUpdateIssuesPerComponent(String providerName, LinkableItem topic, Optional<LinkableItem> subTopic, Collection<ComponentItem> componentItems, JiraIssueConfig jiraIssueConfig) throws IntegrationException {
        Set<String> issueKeys = new HashSet<>();
        SetMap<String, String> missingTransitionToIssues = new SetMap<>();

        ProjectComponent jiraProject = jiraIssueConfig.getProjectComponent();
        String jiraProjectId = jiraProject.getId();
        String jiraProjectName = jiraProject.getName();
        String jiraProjectKey = jiraProject.getKey();

        Map<String, List<ComponentItem>> combinedItemsMap = combineComponentItems(componentItems);
        for (List<ComponentItem> combinedItems : combinedItemsMap.values()) {
            try {
                final ComponentItem arbitraryItem = combinedItems
                                                        .stream()
                                                        .findAny()
                                                        .orElseThrow(() -> new AlertException("Unable to successfully combine component items for Jira Cloud issue handling."));
                final ItemOperation operation = arbitraryItem.getOperation();
                final String trackingKey = createAdditionalTrackingKey(arbitraryItem);
                final IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(arbitraryItem, combinedItems, topic, subTopic, providerName);

                final Optional<IssueComponent> existingIssueComponent = retrieveExistingIssue(jiraProjectKey, providerName, topic, subTopic, arbitraryItem, trackingKey);
                logJiraCloudAction(operation, jiraProjectName, providerName, topic, subTopic, arbitraryItem);
                if (existingIssueComponent.isPresent()) {
                    final IssueComponent issueComponent = existingIssueComponent.get();
                    if (jiraIssueConfig.getCommentOnIssues()) {
                        String operationComment = createOperationComment(operation, arbitraryItem.getCategory(), providerName, combinedItems);
                        addComment(issueComponent.getKey(), operationComment);
                        issueKeys.add(issueComponent.getKey());
                    }

                    boolean didUpdateIssue = jiraTransitionHelper.transitionIssueIfNecessary(issueComponent.getKey(), jiraIssueConfig, operation);
                    if (didUpdateIssue) {
                        issueKeys.add(issueComponent.getKey());
                    }
                } else {
                    if (ItemOperation.ADD.equals(operation) || ItemOperation.UPDATE.equals(operation)) {
                        fieldsBuilder.setProject(jiraProjectId);
                        fieldsBuilder.setIssueType(jiraIssueConfig.getIssueType());
                        String issueCreator = jiraIssueConfig.getIssueCreator();
                        IssueResponseModel issue = null;
                        try {
                            issue = issueService.createIssue(new IssueCreationRequestModel(issueCreator, jiraIssueConfig.getIssueType(), jiraProjectName, fieldsBuilder, List.of()));
                            logger.debug("Created new Jira Cloud issue: {}", issue.getKey());
                        } catch (IntegrationRestException e) {
                            handleIssueCreationRestException(e, issueCreator);
                        }
                        if (issue == null || StringUtils.isBlank(issue.getKey())) {
                            throw new AlertException("There was an problem when creating this issue.");
                        }
                        final String issueKey = issue.getKey();
                        addIssueProperties(issueKey, providerName, topic, subTopic, arbitraryItem, trackingKey);
                        addComment(issueKey, "This issue was automatically created by Alert.");
                        issueKeys.add(issueKey);
                    } else {
                        logger.warn("Expected to find an existing issue with key '{}' but none existed.", trackingKey);
                    }
                }
            } catch (JiraMissingTransitionException e) {
                missingTransitionToIssues.add(e.getTransition(), e.getIssueKey());
            }
        }
        if (!missingTransitionToIssues.isEmpty()) {
            final StringBuilder missingTransitions = new StringBuilder();
            for (Map.Entry<String, Set<String>> entry : missingTransitionToIssues.entrySet()) {
                String issues = StringUtils.join(entry.getValue(), ", ");
                missingTransitions.append(String.format("Unable to find the transition: %s, for the issue(s): %s", entry.getKey(), issues));
            }

            String errorMessage = String.format("For Provider: %s. Project: %s. %s.", providerName, jiraProjectName,
                missingTransitions.toString());
            throw new AlertException(errorMessage);
        }

        return issueKeys;
    }

    private Map<String, List<ComponentItem>> combineComponentItems(Collection<ComponentItem> componentItems) {
        final Map<String, List<ComponentItem>> combinedItems = new LinkedHashMap<>();
        componentItems.forEach(item -> {
            String key = item.getComponentKeys().getShallowKey() + item.getOperation();
            // FIXME find a way to make this provider-agnostic
            if (!item.getCategory().contains("Vuln")) {
                key = item.getComponentKeys().getDeepKey() + item.getOperation();
            }
            combinedItems.computeIfAbsent(key, ignored -> new LinkedList<>()).add(item);
        });
        return combinedItems;
    }

    private Optional<IssueComponent> retrieveExistingIssue(String jiraProjectKey, String provider, LinkableItem topic, Optional<LinkableItem> subTopic, ComponentItem componentItem, String alertIssueUniqueId) throws IntegrationException {
        final Optional<IssueSearchResponseModel> optionalIssueSearchResponse = jiraIssuePropertyHelper.findIssues(jiraProjectKey, provider, topic, subTopic.orElse(null), componentItem, alertIssueUniqueId);
        return optionalIssueSearchResponse
                   .map(IssueSearchResponseModel::getIssues)
                   .map(List::stream)
                   .flatMap(Stream::findFirst);
    }

    private void handleIssueCreationRestException(IntegrationRestException restException, String issueCreatorEmail) throws IntegrationException {
        final JsonObject responseContent = gson.fromJson(restException.getHttpResponseContent(), JsonObject.class);
        List<String> responseErrors = new ArrayList<>();
        if (null != responseContent) {
            JsonObject errors = responseContent.get("errors").getAsJsonObject();
            JsonElement reporterErrorMessage = errors.get("reporter");
            if (null != reporterErrorMessage) {
                throw AlertFieldException.singleFieldError(
                    JiraDescriptor.KEY_ISSUE_CREATOR, String.format("There was a problem assigning '%s' to the issue. Please ensure that the user is assigned to the project and has permission to transition issues.", issueCreatorEmail)
                );
            }

            JsonArray errorMessages = responseContent.get("errorMessages").getAsJsonArray();
            for (JsonElement errorMessage : errorMessages) {
                responseErrors.add(errorMessage.getAsString());
            }
            responseErrors.add(errors.toString());
        }

        String message = restException.getMessage();
        if (!responseErrors.isEmpty()) {
            message += " | Details: " + StringUtils.join(responseErrors, ", ");
        }

        throw new AlertException(message, restException);
    }

    private void addIssueProperties(String issueKey, String provider, LinkableItem topic, Optional<LinkableItem> subTopic, ComponentItem componentItem, String alertIssueUniqueId)
        throws IntegrationException {
        final LinkableItem component = componentItem.getComponent();
        final Optional<LinkableItem> subComponent = componentItem.getSubComponent();

        jiraIssuePropertyHelper.addPropertiesToIssue(issueKey, provider,
            topic.getName(), topic.getValue(), subTopic.map(LinkableItem::getName).orElse(null), subTopic.map(LinkableItem::getValue).orElse(null),
            componentItem.getCategory(),
            component.getName(), component.getValue(), subComponent.map(LinkableItem::getName).orElse(null), subComponent.map(LinkableItem::getValue).orElse(null),
            alertIssueUniqueId
        );
    }

    private String createOperationComment(ItemOperation operation, String category, String provider, Collection<ComponentItem> componentItems) {
        JiraIssueFormatHelper jiraChannelFormatHelper = new JiraIssueFormatHelper();
        String attributesString = jiraChannelFormatHelper.createComponentAttributesString(componentItems);

        StringBuilder commentBuilder = new StringBuilder();
        commentBuilder.append("The ");
        commentBuilder.append(operation.name());
        commentBuilder.append(" operation was performed for this ");
        commentBuilder.append(category);
        commentBuilder.append(" in ");
        commentBuilder.append(provider);
        if (StringUtils.isNotBlank(attributesString)) {
            commentBuilder.append(".\n----------\n");
            commentBuilder.append(attributesString);
        } else {
            commentBuilder.append(".");
        }
        return commentBuilder.toString();
    }

    private IssueRequestModelFieldsBuilder createFieldsBuilder(ComponentItem arbitraryItem, Collection<ComponentItem> componentItems, LinkableItem commonTopic, Optional<LinkableItem> subTopic, String provider) {
        final JiraIssueFormatHelper jiraChannelFormatHelper = new JiraIssueFormatHelper();
        final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        final String title = jiraChannelFormatHelper.createTitle(provider, commonTopic, subTopic, arbitraryItem.getComponentKeys());
        final String description = jiraChannelFormatHelper.createDescription(commonTopic, subTopic, componentItems, provider);
        fieldsBuilder.setSummary(title);
        fieldsBuilder.setDescription(description);

        return fieldsBuilder;
    }

    private void addComment(String issueKey, String comment) throws IntegrationException {
        final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
        issueService.addComment(issueCommentRequestModel);
    }

    private String createAdditionalTrackingKey(ComponentItem componentItem) {
        // FIXME make this provider-agnostic
        if (componentItem.getCategory().contains("Vuln")) {
            return StringUtils.EMPTY;
        }
        StringBuilder keyBuilder = new StringBuilder();
        final Map<String, List<LinkableItem>> itemsOfSameName = componentItem.getItemsOfSameName();
        for (List<LinkableItem> componentAttributeList : itemsOfSameName.values()) {
            componentAttributeList
                .stream()
                .findFirst()
                .filter(LinkableItem::isPartOfKey)
                .ifPresent(item -> {
                    keyBuilder.append(item.getName());
                    keyBuilder.append(item.getValue());
                });
        }
        return keyBuilder.toString();
    }

    private String createStatusMessage(Collection<String> issueKeys, String jiraUrl) {
        if (issueKeys.isEmpty()) {
            return "Did not create any Jira Cloud issues.";
        }
        final String concatenatedKeys = String.join(", ", issueKeys);
        logger.debug("Issues updated: {}", concatenatedKeys);
        return String.format("Successfully created Jira Cloud issue at %s/issues/?jql=issuekey in (%s)", jiraUrl, concatenatedKeys);
    }

    private void logJiraCloudAction(ItemOperation operation, String jiraProjectName, String providerName, LinkableItem topic, Optional<LinkableItem> subTopic, ComponentItem arbitraryItem) {
        String jiraProjectVersion = subTopic.map(LinkableItem::getValue).orElse("unknown");
        String arbitraryItemSubComponent = arbitraryItem.getSubComponent().map(LinkableItem::getValue).orElse("unknown");
        logger.debug("Attempting the {} action on the Jira Cloud project {}. Provider: {}, Provider Project: {}[{}]. Category: {}, Component: {}, SubComponent: {}.",
            operation.name(), jiraProjectName, providerName, topic.getValue(), jiraProjectVersion, arbitraryItem.getCategory(), arbitraryItem.getComponent().getName(), arbitraryItemSubComponent);
    }

}
