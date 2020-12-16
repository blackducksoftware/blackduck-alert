/**
 * channel
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
package com.synopsys.integration.alert.channel.jira.common.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.model.JiraCustomFieldConfig;
import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
import com.synopsys.integration.alert.channel.jira.common.model.JiraResolvedCustomField;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueHandler;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class JiraIssueHandler extends IssueHandler<IssueResponseModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCustomFieldResolver jiraCustomFieldResolver;
    private final JiraTransitionHandler jiraTransitionHelper;
    private final JiraIssuePropertyHandler jiraIssuePropertyHelper;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;

    public JiraIssueHandler(JiraErrorMessageUtility jiraErrorMessageUtility, JiraCustomFieldResolver jiraCustomFieldResolver, JiraTransitionHandler jiraTransitionHandler, JiraIssuePropertyHandler<?> jiraIssuePropertyHandler,
        JiraContentValidator contentValidator) {
        super(contentValidator);
        this.jiraCustomFieldResolver = jiraCustomFieldResolver;
        this.jiraTransitionHelper = jiraTransitionHandler;
        this.jiraIssuePropertyHelper = jiraIssuePropertyHandler;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
    }

    public abstract IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException;

    public abstract String getIssueCreatorFieldKey();

    @Override
    // TODO this does not need to be Optional
    protected Optional<IssueResponseModel> createIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        JiraIssueSearchProperties issueSearchProperties = request.getIssueSearchProperties();
        IssueContentModel contentModel = request.getRequestContent();

        IssueContentModel issueContentModel = contentModel;
        if (!contentModel.getDescriptionComments().isEmpty() && !issueConfig.getCommentOnIssues()) {
            String description = truncateDescription(contentModel.getDescription());
            issueContentModel = IssueContentModel.of(contentModel.getTitle(), description, List.of());
        }

        IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(issueContentModel);
        appendIssueConfig(fieldsBuilder, (JiraIssueConfig) issueConfig, issueSearchProperties);

        String issueCreator = issueConfig.getIssueCreator();
        try {
            IssueResponseModel issue = createIssue(issueCreator, issueConfig.getIssueType(), issueConfig.getProjectName(), fieldsBuilder);
            logger.debug("Created new Jira Cloud issue: {}", issue.getKey());
            String issueKey = issue.getKey();
            addIssueProperties(issueKey, issueSearchProperties);
            if (issueConfig.getCommentOnIssues()) {
                addComment(issueConfig, issueKey, "This issue was automatically created by Alert.");
                for (String additionalComment : issueContentModel.getDescriptionComments()) {
                    String comment = String.format("%s \n %s", DESCRIPTION_CONTINUED_TEXT, additionalComment);
                    addComment(issueConfig, issueKey, comment);
                }
            }
            return Optional.of(issue);
        } catch (IntegrationRestException e) {
            AlertException improvedException = jiraErrorMessageUtility.improveRestException(e, getIssueCreatorFieldKey(), issueCreator);
            logger.error("Error creating issue", improvedException);
            throw improvedException;
        }
    }

    @Override
    protected void logIssueAction(String issueTrackerProjectName, IssueTrackerRequest request) {
        JiraIssueSearchProperties issueProperties = request.getIssueSearchProperties();
        String issueTrackerProjectVersion = issueProperties.getSubTopicValue() != null ? issueProperties.getSubTopicValue() : "unknown";
        String arbitraryItemSubComponent = issueProperties.getSubComponentValue() != null ? issueProperties.getSubTopicValue() : "unknown";
        logger.debug("Attempting the {} action on the project {}. Provider: {}, Provider URL: {}, Provider Project: {}[{}]. Category: {}, Component: {}, SubComponent: {}.",
            request.getOperation().name(), issueTrackerProjectName, issueProperties.getProvider(), issueProperties.getProviderUrl(), issueProperties.getTopicValue(), issueTrackerProjectVersion, issueProperties.getCategory(),
            issueProperties.getComponentValue(),
            arbitraryItemSubComponent);
    }

    @Override
    protected boolean transitionIssue(IssueResponseModel issueResponseModel, IssueConfig issueConfig, IssueOperation operation) throws IntegrationException {
        return jiraTransitionHelper.transitionIssueIfNecessary(issueResponseModel.getKey(), issueConfig, operation);
    }

    private void addIssueProperties(String issueKey, JiraIssueSearchProperties issueProperties) throws IntegrationException {
        jiraIssuePropertyHelper.addPropertiesToIssue(issueKey, issueProperties);
    }

    private IssueRequestModelFieldsBuilder createFieldsBuilder(IssueContentModel contentModel) {
        IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        fieldsBuilder.setSummary(contentModel.getTitle());
        fieldsBuilder.setDescription(contentModel.getDescription());
        return fieldsBuilder;
    }

    private void appendIssueConfig(IssueRequestModelFieldsBuilder fieldsBuilder, JiraIssueConfig issueConfig, JiraIssueSearchProperties issueSearchProperties) {
        fieldsBuilder.setProject(issueConfig.getProjectId());
        fieldsBuilder.setIssueType(issueConfig.getIssueType());

        for (JiraCustomFieldConfig customField : issueConfig.getCustomFields()) {
            JiraCustomFieldValueReplacementUtils.injectReplacementFieldValue(customField, issueSearchProperties);
            JiraResolvedCustomField resolvedCustomField = jiraCustomFieldResolver.resolveCustomField(customField);
            fieldsBuilder.setValue(resolvedCustomField.getFieldId(), resolvedCustomField.getFieldValue());
        }
    }

}
