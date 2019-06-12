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
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
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
import com.synopsys.integration.jira.common.cloud.model.components.IssueComponent;
import com.synopsys.integration.jira.common.cloud.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.cloud.rest.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.rest.service.ProjectService;

@Component(value = JiraChannel.COMPONENT_NAME)
public class JiraChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_jira_cloud";
    private static final Logger logger = LoggerFactory.getLogger(JiraChannel.class);

    public JiraChannel(final Gson gson, final AlertProperties alertProperties, final AuditUtility auditUtility) {
        super(gson, alertProperties, auditUtility);
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
        final String projectId = projectsResponseModel.getProjects()
                                     .stream()
                                     .findFirst()
                                     .map(ProjectComponent::getId)
                                     .orElseThrow(() -> new AlertException("Expected to be passed an existing project name."));
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
        final LinkableItem commonTopic = content.getCommonTopic();
        final String commonTopicName = commonTopic.getName();
        final String commonTopicValue = commonTopic.getValue();
        for (final AggregateMessageContent messageContent : content.getSubContent()) {
            final Optional<LinkableItem> subTopic = messageContent.getSubTopic();
            final String messageName = messageContent.getName();
            final String messageValue = messageContent.getValue();
            for (final CategoryItem categoryItem : messageContent.getCategoryItems()) {
                final IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(content, issueType, projectId);
                final String trackingComment = createTrackingComment(categoryItem, providerName);
                final Optional<IssueComponent> existingIssueComponent = retrieveExistingIssue(issueSearchService, trackingComment);
                if (existingIssueComponent.isPresent()) {
                    final IssueComponent issueComponent = existingIssueComponent.get();
                    issueKeys.add(issueComponent.getKey());
                    updateIssue(issueService, fieldsBuilder, issueComponent);
                } else {
                    final String username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_USERNAME).orElseThrow(() -> new AlertException("Expected to be passed a jira username."));
                    final IssueResponseModel issue = issueService.createIssue(new IssueCreationRequestModel(username, issueType, projectName, fieldsBuilder, List.of()));
                    if (issue == null || StringUtils.isBlank(issue.getKey())) {
                        throw new AlertException("There was an problem when creating this issue.");
                    }
                    final String issueKey = issue.getKey();
                    addCreationComment(issueService, issueKey, trackingComment);
                    issueKeys.add(issueKey);
                }

            }
        }
        return createSuccessMessage(issueKeys, jiraProperties.getUrl());
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }

    private Optional<IssueComponent> retrieveExistingIssue(final IssueSearchService issueSearchService, final String commentToSearchFor) throws IntegrationException {
        final IssueSearchResponseModel issuesByComment = issueSearchService.findIssuesByComment(commentToSearchFor);
        return issuesByComment.getIssues().stream().findFirst();
    }

    // TODO create the content of the Jira issue.
    private IssueRequestModelFieldsBuilder createFieldsBuilder(final MessageContentGroup message, final String issueType, final String projectId) {
        final IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        final List<AggregateMessageContent> subContent = message.getSubContent();
        final String description = subContent.stream().findFirst().flatMap(AggregateMessageContent::getSubTopic).map(LinkableItem::getValue).orElse("Empty");
        final String title = message.getCommonTopic().getValue();
        fieldsBuilder.setSummary(title);
        fieldsBuilder.setDescription(description);
        fieldsBuilder.setIssueType(issueType);
        fieldsBuilder.setProject(projectId);

        return fieldsBuilder;
    }

    private void updateIssue(final IssueService issueService, final IssueRequestModelFieldsBuilder fieldsBuilder, final IssueComponent issueComponent) throws IntegrationException {
        issueService.updateIssue(new IssueRequestModel(fieldsBuilder, Map.of(), List.of()));
    }

    private void addCreationComment(final IssueService issueService, final String issueKey, final String comment) throws IntegrationException {
        final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, String.format("Alert key (DO NOT DELETE): %s", comment));
        issueService.addComment(issueCommentRequestModel);
    }

    private String createTrackingComment(final CategoryItem categoryItem, final String providerName) {
        final CategoryKey categoryKey = categoryItem.getCategoryKey();
        return String.format("%s:%s", providerName, categoryKey.getKey());
    }

    private String createSuccessMessage(final Collection<String> issueKeys, final String jiraUrl) {
        final String concatenatedKeys = issueKeys.stream().collect(Collectors.joining(","));
        return String.format("Successfully created Jira Cloud issue at %s/issues/?jql=issuekey in (%s)", jiraUrl, concatenatedKeys);
    }
}
