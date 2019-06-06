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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDistributionUIConfig;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.cloud.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.cloud.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.cloud.model.response.IssueResponseModel;
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
        final IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(content, issueType, projectId);
        final Optional<IssueResponseModel> existingIssueResponseModel = retrieveExistingIssue(issueSearchService);
        final String issueKey;
        if (existingIssueResponseModel.isPresent()) {
            final IssueResponseModel issueResponseModel = existingIssueResponseModel.get();
            issueKey = issueResponseModel.getKey();

        } else {
            final String username = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_USERNAME).orElseThrow(() -> new AlertException("Expected to be passed a jira username."));
            final IssueResponseModel issue = issueService.createIssue(new IssueCreationRequestModel(username, issueType, projectName, fieldsBuilder, List.of()));
            if (issue == null || StringUtils.isBlank(issue.getKey())) {
                throw new AlertException("There was an problem when creating this issue.");
            }
            issueKey = issue.getKey();
            addCreationComment(issueService, issueKey, content);
        }
        return String.format("Successfully created Jira Cloud issue at %s/browse/%s", jiraProperties.getUrl(), issueKey);
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }

    // TODO Use the issue search service to check if an issue already exists
    private Optional<IssueResponseModel> retrieveExistingIssue(final IssueSearchService issueSearchService) throws IntegrationException {
        return Optional.empty();
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

    private void updateIssue(final IssueService issueService, final IssueRequestModelFieldsBuilder fieldsBuilder, final IssueResponseModel issueResponseModel) throws IntegrationException {
        final IssueFieldsComponent fields = issueResponseModel.getFields();
        issueService.updateIssue(new IssueRequestModel(fieldsBuilder, Map.of(), List.of()));
    }

    // TODO hash the comment value so that we can reference it again in the future.
    private void addCreationComment(final IssueService issueService, final String issueKey, final MessageContentGroup message) throws IntegrationException {
        final String value = message.getCommonTopic().getValue();
        final String commentToHash = String.format("%s:%s", COMPONENT_NAME, value);
        final IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, String.format("Alert key (DO NOT DELETE): %s", commentToHash), null, null, List.of());
        issueService.addComment(issueCommentRequestModel);
    }
}
