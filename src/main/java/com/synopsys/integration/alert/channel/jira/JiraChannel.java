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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDistributionUIConfig;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
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
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.rest.service.ProjectService;

@Component(value = JiraChannel.COMPONENT_NAME)
public class JiraChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_jira_cloud";
    private static final Logger logger = LoggerFactory.getLogger(JiraChannel.class);

    public JiraChannel(final Gson gson, final AuditUtility auditUtility) {
        super(gson, auditUtility);
    }

    @Override
    public String sendMessage(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fieldAccessor = event.getFieldAccessor();
        final MessageContentGroup content = event.getContent();
        final JiraProperties jiraProperties = new JiraProperties(fieldAccessor);
        final JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, getGson());
        final IssueService issueService = jiraCloudServiceFactory.createIssueService();
        final IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        final ProjectService projectService = jiraCloudServiceFactory.createProjectService();
        final String projectName = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_PROJECT_NAME).orElse("");
        final PageOfProjectsResponseModel projectsResponseModel = projectService.getProjectsByName(projectName);
        final ProjectComponent projectComponent = projectsResponseModel.getProjects()
                                                      .stream()
                                                      .findFirst()
                                                      .orElseThrow(() -> new AlertException("Expected to be passed an existing project name."));
        final String projectId = projectComponent.getId();
        final String projectKey = projectComponent.getKey();
        final String issueType = fieldAccessor.getString(JiraDescriptor.KEY_ISSUE_TYPE).orElse(JiraDistributionUIConfig.DEFAULT_ISSUE_TYPE);
        final IssueTypeService issueTypeService = jiraCloudServiceFactory.createIssueTypeService();
        final boolean matchingIssueTypeFound = issueTypeService.getAllIssueTypes()
                                                   .stream()
                                                   .map(IssueTypeResponseModel::getName)
                                                   .anyMatch(name -> name.equals(issueType));
        if (!matchingIssueTypeFound) {
            throw new AlertException("Could not find Issue type on Jira Cloud instance: " + issueType);
        }

        final Set<String> issueKeys = new HashSet<>();
        final String providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElseThrow(() -> new AlertException("Expected to be passed a provider."));
        final Boolean commentOnIssue = fieldAccessor.getBoolean(JiraDescriptor.KEY_ADD_COMMENTS).orElse(false);
        final LinkableItem commonTopic = content.getCommonTopic();
        for (final AggregateMessageContent messageContent : content.getSubContent()) {
            final Optional<LinkableItem> subTopic = messageContent.getSubTopic();
            for (final CategoryItem categoryItem : messageContent.getCategoryItems()) {
                final ItemOperation operation = categoryItem.getOperation();
                final String trackingMessage = createTrackingKey(categoryItem, providerName);
                final Optional<IssueComponent> existingIssueComponent = retrieveExistingIssue(issueSearchService, projectKey, issueType, trackingMessage);
                if (existingIssueComponent.isPresent()) {
                    final IssueComponent issueComponent = existingIssueComponent.get();
                    if (ItemOperation.DELETE.equals(operation)) {
                        final Optional<String> resolveTransition = fieldAccessor.getString(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);
                        final String issueKey = transitionIssue(issueService, issueComponent, resolveTransition);
                        issueKeys.add(issueKey);
                        if (commentOnIssue) {
                            // FIXME update this code to be provider specific and provider useful information.
                            final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, "DELETED PLACEHOLDER TEXT");
                            issueService.addComment(issueCommentRequestModel);
                        }
                    } else if (ItemOperation.ADD.equals(operation) || ItemOperation.UPDATE.equals(operation)) {
                        final Optional<String> openTransition = fieldAccessor.getString(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION);
                        final String issueKey = transitionIssue(issueService, issueComponent, openTransition);
                        issueKeys.add(issueKey);
                        if (commentOnIssue) {
                            // FIXME update this code to be provider specific and provider useful information.
                            final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, "ADDED/UPDATED PLACEHOLDER TEXT");
                            issueService.addComment(issueCommentRequestModel);
                        }
                    }
                } else {
                    if (ItemOperation.ADD.equals(operation) || ItemOperation.UPDATE.equals(operation)) {
                        final String username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_USERNAME).orElseThrow(() -> new AlertException("Expected to be passed a jira username."));
                        final IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(categoryItem, commonTopic, subTopic, issueType, projectId, providerName);
                        final IssueResponseModel issue = issueService.createIssue(new IssueCreationRequestModel(username, issueType, projectName, fieldsBuilder, List.of()));
                        if (issue == null || StringUtils.isBlank(issue.getKey())) {
                            throw new AlertException("There was an problem when creating this issue.");
                        }
                        final String issueKey = issue.getKey();
                        addCreationComment(issueService, issueKey);
                        issueKeys.add(issueKey);
                    } else {
                        logger.warn("Expected to find an existing issue with key '{}' but none existed.", trackingMessage);
                    }
                }
            }
        }
        return createSuccessMessage(issueKeys, jiraProperties.getUrl());
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }

    private String transitionIssue(final IssueService issueService, final IssueComponent issueComponent, final Optional<String> optionalTransition) throws IntegrationException {
        final String key = issueComponent.getKey();
        if (optionalTransition.isPresent()) {
            final TransitionsResponseModel transitions = issueService.getTransitions(key);
            final String transition = optionalTransition.get();
            final Optional<TransitionComponent> firstTransitionByName = transitions.findFirstTransitionByName(transition);
            if (firstTransitionByName.isPresent()) {
                final TransitionComponent issueTransition = firstTransitionByName.get();
                final String transitionId = issueTransition.getId();
                final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
                final IssueRequestModel issueRequestModel = new IssueRequestModel(key, new IdComponent(transitionId), fieldsBuilder, Map.of(), List.of());
                issueService.transitionIssue(issueRequestModel);
            } else {
                logger.warn("Was unable to find given transition {}.", transition);
            }
        } else {
            logger.debug("No transition selected, ignoring issue state change.");
        }

        return key;
    }

    private Optional<IssueComponent> retrieveExistingIssue(final IssueSearchService issueSearchService, final String projectKey, final String issueType, final String itemToSearchFor) throws IntegrationException {
        final IssueSearchResponseModel issuesByDescription = issueSearchService.findIssuesByDescription(projectKey, issueType, itemToSearchFor);
        return issuesByDescription.getIssues().stream().findFirst();
    }

    private IssueRequestModelFieldsBuilder createFieldsBuilder(final CategoryItem categoryItem, final LinkableItem commonTopic, final Optional<LinkableItem> subTopic, final String issueType, final String projectId, final String provider) {
        final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        final String title = createTitle(provider, commonTopic, subTopic, categoryItem);
        final String description = createDescription(commonTopic, subTopic, categoryItem, provider);
        fieldsBuilder.setSummary(title);
        fieldsBuilder.setDescription(description);
        fieldsBuilder.setIssueType(issueType);
        fieldsBuilder.setProject(projectId);

        return fieldsBuilder;
    }

    private String createTitle(final String provider, final LinkableItem commonTopic, final Optional<LinkableItem> subTopic, final CategoryItem categoryItem) {
        final StringBuilder title = new StringBuilder();
        title.append("Alert: Provider|");
        title.append(provider);
        title.append(", ");
        title.append(commonTopic.getName());
        title.append("|");
        title.append(commonTopic.getValue());
        title.append(", ");

        if (subTopic.isPresent()) {
            final LinkableItem subTopicItem = subTopic.get();
            title.append(subTopicItem.getName());
            title.append("|");
            title.append(subTopicItem.getValue());
            title.append(", ");
        }

        // FIXME this needs to be changed to contain appropriate values from the category item instead of just the key.
        final String categoryKey = categoryItem.getCategoryKey().getKey();
        title.append(categoryKey);
        return title.toString();
    }

    private String createDescription(final LinkableItem commonTopic, final Optional<LinkableItem> subTopic, final CategoryItem categoryItem, final String providerName) {
        final StringBuilder description = new StringBuilder();
        description.append(commonTopic.getName());
        description.append(":");
        description.append(commonTopic.getValue());
        description.append("\n");
        if (subTopic.isPresent()) {
            final LinkableItem linkableItem = subTopic.get();
            description.append(linkableItem.getName());
            description.append(": ");
            final String value = linkableItem.getValue();
            final Optional<String> optionalUrl = linkableItem.getUrl();
            if (optionalUrl.isPresent()) {
                // FIXME the URL is not properly being created
                final String url = optionalUrl.get();
                description.append("[");
                description.append(value);
                description.append("](");
                description.append(url);
                description.append(")");
            } else {
                description.append(value);
            }
            description.append("\n");
        }
        description.append(createDescriptionItems(categoryItem));

        description.append("\n------------------- Generated key for Alert. DO NOT DELETE OR EDIT -------------------\nGenerated key: ");
        description.append(createTrackingKey(categoryItem, providerName));
        description.append("\n----------------------------------------------------------------------------------------");

        return description.toString();
    }

    private String createDescriptionItems(final CategoryItem categoryItem) {
        final StringBuilder description = new StringBuilder();
        final Map<String, List<LinkableItem>> itemsOfSameName = categoryItem.getItemsOfSameName();

        for (final Map.Entry<String, List<LinkableItem>> entry : itemsOfSameName.entrySet()) {
            final String itemName = entry.getKey();
            final List<String> itemValues = entry.getValue().stream().map(LinkableItem::getValue).collect(Collectors.toList());
            description.append(itemName);
            description.append(":");
            if (itemValues.size() > 1) {
                for (final String value : itemValues) {
                    description.append("[");
                    description.append(value);
                    description.append("]");
                }
            } else {
                for (final String value : itemValues) {
                    description.append(value);
                }
            }
            description.append("\n");
        }

        return description.toString();
    }

    private void addCreationComment(final IssueService issueService, final String issueKey) throws IntegrationException {
        final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, "This issue has been created by Alert.");
        issueService.addComment(issueCommentRequestModel);
    }

    private String createTrackingKey(final CategoryItem categoryItem, final String providerName) {
        /*
         FIXME category key won't work as tracking key (Policy override generates a different key than policy violation and thus won't update issues correctly).
          provider_blackduck:1.2.1_Apache Commons FileUpload_Component_Component Version_Policy Overridden by_System Administrator'
          vs
          provider_blackduck:1.2.1_Apache Commons FileUpload_Component_Component Version
        */
        final CategoryKey categoryKey = categoryItem.getCategoryKey();
        final String operationName = categoryItem.getOperation().name();
        final String operationRemovedKey = categoryKey.getKey().replace("_" + operationName, "");
        return String.format("%s:%s", providerName, operationRemovedKey);
    }

    private String createSuccessMessage(final Collection<String> issueKeys, final String jiraUrl) {
        final String concatenatedKeys = issueKeys.stream().collect(Collectors.joining(","));
        return String.format("Successfully created Jira Cloud issue at %s/issues/?jql=issuekey in (%s)", jiraUrl, concatenatedKeys);
    }
}
