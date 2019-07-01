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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDistributionUIConfig;
import com.synopsys.integration.alert.channel.jira.util.JiraIssueFormatHelper;
import com.synopsys.integration.alert.channel.jira.util.JiraIssuePropertyHelper;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.model.components.IdComponent;
import com.synopsys.integration.jira.common.cloud.model.components.IssueComponent;
import com.synopsys.integration.jira.common.cloud.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.cloud.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.cloud.rest.service.ProjectService;

public class JiraIssueHandler {
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueHandler.class);

    private final ProjectService projectService;
    private final IssueService issueService;
    private final IssueTypeService issueTypeService;
    private final JiraProperties jiraProperties;

    private final JiraIssuePropertyHelper jiraIssuePropertyHelper;

    public JiraIssueHandler(
        final ProjectService projectService, final IssueService issueService, final IssueSearchService issueSearchService, final IssuePropertyService issuePropertyService, final IssueTypeService issueTypeService,
        final JiraProperties jiraProperties) {
        this.projectService = projectService;
        this.issueService = issueService;
        this.issueTypeService = issueTypeService;
        this.jiraProperties = jiraProperties;
        this.jiraIssuePropertyHelper = new JiraIssuePropertyHelper(issueSearchService, issuePropertyService);
    }

    public String createOrUpdateIssues(final FieldAccessor fieldAccessor, final MessageContentGroup content) throws IntegrationException {
        final String projectName = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_PROJECT_NAME).orElse("");
        final PageOfProjectsResponseModel projectsResponseModel = projectService.getProjectsByName(projectName);
        final ProjectComponent projectComponent = projectsResponseModel.getProjects()
                                                      .stream()
                                                      .findFirst()
                                                      .orElseThrow(() -> new AlertException(String.format("No projects matching '%s' were found", projectName)));
        final String issueType = fieldAccessor.getString(JiraDescriptor.KEY_ISSUE_TYPE).orElse(JiraDistributionUIConfig.DEFAULT_ISSUE_TYPE);
        issueTypeService.getAllIssueTypes()
            .stream()
            .map(IssueTypeResponseModel::getName)
            .filter(name -> name.equals(issueType))
            .findAny()
            .orElseThrow(() -> new AlertException(String.format("The issue type '%s' could not be found", issueType)));

        final String projectId = projectComponent.getId();
        final String providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElseThrow(() -> new AlertException("Expected to be passed a provider."));
        final Boolean commentOnIssue = fieldAccessor.getBoolean(JiraDescriptor.KEY_ADD_COMMENTS).orElse(false);
        final LinkableItem commonTopic = content.getCommonTopic();

        final Set<String> issueKeys = new HashSet<>();
        for (final ProviderMessageContent messageContent : content.getSubContent()) {
            final Optional<LinkableItem> subTopic = messageContent.getSubTopic();

            final Set<String> issueKeysForMessage = createOrUpdateIssuesPerComponent(providerName, commonTopic, subTopic, fieldAccessor, messageContent.getComponentItems(), issueType, projectName, projectId, commentOnIssue);
            issueKeys.addAll(issueKeysForMessage);
        }

        return createStatusMessage(issueKeys, jiraProperties.getUrl());
    }

    private Set<String> createOrUpdateIssuesPerComponent(final String providerName, final LinkableItem topic, final Optional<LinkableItem> subTopic, final FieldAccessor fieldAccessor, final Collection<ComponentItem> componentItems,
        final String issueType, final String projectName, final String projectId, final Boolean commentOnIssue) throws IntegrationException {
        final Set<String> issueKeys = new HashSet<>();
        final Map<String, List<ComponentItem>> combinedItemsMap = combineComponentItems(componentItems);
        for (final List<ComponentItem> combinedItems : combinedItemsMap.values()) {
            final ComponentItem arbitraryItem = combinedItems
                                                    .stream()
                                                    .findAny()
                                                    .orElseThrow(() -> new AlertException("Unable to successfully combine component items for Jira Cloud issue handling."));
            final ItemOperation operation = arbitraryItem.getOperation();
            final String trackingKey = createAdditionalTrackingKey(arbitraryItem);

            final String operationComment = String.format("The %s operation was performed for this %s in %s", operation.name(), arbitraryItem.getCategory(), providerName);
            final Optional<IssueComponent> existingIssueComponent = retrieveExistingIssue(providerName, topic, subTopic, arbitraryItem, trackingKey);
            if (existingIssueComponent.isPresent()) {
                final IssueComponent issueComponent = existingIssueComponent.get();
                final String transitionKey = (ItemOperation.DELETE.equals(operation)) ? JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION : JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
                final String issueKey = transitionIssue(issueComponent.getKey(), fieldAccessor, transitionKey);
                issueKeys.add(issueKey);
                if (commentOnIssue) {
                    addComment(issueKey, operationComment);
                }
            } else {
                if (ItemOperation.ADD.equals(operation) || ItemOperation.UPDATE.equals(operation)) {
                    final String username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_USERNAME).orElseThrow(() -> new AlertException("Expected to be passed a jira username."));
                    final IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(arbitraryItem, combinedItems, topic, subTopic, issueType, projectId, providerName);
                    final IssueResponseModel issue = issueService.createIssue(new IssueCreationRequestModel(username, issueType, projectName, fieldsBuilder, List.of()));
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
        }
        return issueKeys;
    }

    private Map<String, List<ComponentItem>> combineComponentItems(Collection<ComponentItem> componentItems) {
        final Map<String, List<ComponentItem>> combinedItems = new LinkedHashMap<>();
        componentItems
            .stream()
            .forEach(item -> {
                String key = item.getComponentKeys().getShallowKey() + item.getOperation();
                // FIXME find a way to make this provider-agnostic
                if (item.getCategory().contains("Policy")) {
                    key = item.getComponentKeys().getDeepKey() + item.getOperation();
                }
                combinedItems.computeIfAbsent(key, ignored -> new LinkedList<>()).add(item);
            });
        return combinedItems;
    }

    private Optional<IssueComponent> retrieveExistingIssue(String provider, LinkableItem topic, Optional<LinkableItem> subTopic, ComponentItem componentItem, String alertIssueUniqueId) throws IntegrationException {
        final Optional<IssueSearchResponseModel> optionalIssueSearchResponse = jiraIssuePropertyHelper.findIssues(provider, topic, subTopic.orElse(null), componentItem, alertIssueUniqueId);
        return optionalIssueSearchResponse
                   .map(IssueSearchResponseModel::getIssues)
                   .map(List::stream)
                   .flatMap(Stream::findFirst);
    }

    private void addIssueProperties(final String issueKey, final String provider, final LinkableItem topic, final Optional<LinkableItem> subTopic, final ComponentItem componentItem, final String alertIssueUniqueId)
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

    private IssueRequestModelFieldsBuilder createFieldsBuilder(
        ComponentItem arbitraryItem, Collection<ComponentItem> componentItems, LinkableItem commonTopic, Optional<LinkableItem> subTopic, String issueType, String projectId, String provider) {
        final JiraIssueFormatHelper jiraChannelFormatHelper = new JiraIssueFormatHelper();
        final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        final String title = jiraChannelFormatHelper.createTitle(provider, commonTopic, subTopic, arbitraryItem.getComponentKeys());
        final String description = jiraChannelFormatHelper.createDescription(commonTopic, subTopic, componentItems, provider);
        fieldsBuilder.setSummary(title);
        fieldsBuilder.setDescription(description);
        fieldsBuilder.setIssueType(issueType);
        fieldsBuilder.setProject(projectId);

        return fieldsBuilder;
    }

    private String transitionIssue(final String issueKey, final FieldAccessor fieldAccessor, final String transitionKey) throws IntegrationException {
        final Optional<String> transitionName = fieldAccessor.getString(transitionKey);
        if (transitionName.isPresent()) {
            final TransitionsResponseModel transitions = issueService.getTransitions(issueKey);
            final String transition = transitionName.get();
            final Optional<TransitionComponent> firstTransitionByName = transitions.findFirstTransitionByName(transition);
            if (firstTransitionByName.isPresent()) {
                final TransitionComponent issueTransition = firstTransitionByName.get();
                final String transitionId = issueTransition.getId();
                final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
                final IssueRequestModel issueRequestModel = new IssueRequestModel(issueKey, new IdComponent(transitionId), fieldsBuilder, Map.of(), List.of());
                issueService.transitionIssue(issueRequestModel);
            } else {
                logger.warn("Was unable to find given transition {}.", transition);
            }
        } else {
            logger.debug("No transition selected, ignoring issue state change.");
        }

        return issueKey;
    }

    private void addComment(final String issueKey, final String comment) throws IntegrationException {
        final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
        issueService.addComment(issueCommentRequestModel);
    }

    private String createAdditionalTrackingKey(ComponentItem componentItem) {
        StringBuilder keyBuilder = new StringBuilder();
        final Map<String, List<LinkableItem>> itemsOfSameName = componentItem.getItemsOfSameName();
        for (final List<LinkableItem> componentAttributeList : itemsOfSameName.values()) {
            if (componentAttributeList.size() == 1) {
                componentAttributeList
                    .stream()
                    .findFirst()
                    .filter(LinkableItem::isPartOfKey)
                    // FIXME make this provider-agnostic
                    .filter(item -> item.getName().contains("Policy"))
                    .ifPresent(item -> {
                        keyBuilder.append(item.getName());
                        keyBuilder.append(item.getValue());
                    });
            }
        }
        return keyBuilder.toString();
    }

    private String createStatusMessage(final Collection<String> issueKeys, final String jiraUrl) {
        if (issueKeys.isEmpty()) {
            return "Did not create any Jira Cloud issues.";
        }
        final String concatenatedKeys = issueKeys.stream().collect(Collectors.joining(","));
        return String.format("Successfully created Jira Cloud issue at %s/issues/?jql=issuekey in (%s)", jiraUrl, concatenatedKeys);
    }

}
