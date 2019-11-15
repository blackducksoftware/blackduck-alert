/**
 * alert-issuetracker
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
package com.synopsys.integration.alert.issuetracker.jira.common.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.issuetracker.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.IssueHandler;
import com.synopsys.integration.alert.issuetracker.IssueProperties;
import com.synopsys.integration.alert.issuetracker.OperationType;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerFieldException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class JiraIssueHandler extends IssueHandler<IssueResponseModel> {
    public static final String DESCRIPTION_CONTINUED_TEXT = "(description continued...)";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraTransitionHandler jiraTransitionHelper;
    private final JiraIssuePropertyHandler jiraIssuePropertyHelper;

    public JiraIssueHandler(Gson gson, JiraTransitionHandler jiraTransitionHandler, JiraIssuePropertyHandler<?> jiraIssuePropertyHandler) {
        super();
        this.gson = gson;
        this.jiraTransitionHelper = jiraTransitionHandler;
        this.jiraIssuePropertyHelper = jiraIssuePropertyHandler;
    }

    public abstract IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException;

    public abstract String getIssueCreatorFieldKey();

    @Override
    protected IssueResponseModel createIssue(IssueConfig issueConfig, IssueContentModel contentModel)
        throws IntegrationException {
        IssueProperties issueProperties = contentModel.getIssueProperties();
        IssueRequestModelFieldsBuilder fieldsBuilder = createFieldsBuilder(contentModel);
        fieldsBuilder.setProject(issueConfig.getProjectId());
        fieldsBuilder.setIssueType(issueConfig.getIssueType());
        String issueCreator = issueConfig.getIssueCreator();

        try {
            IssueResponseModel issue = createIssue(issueCreator, issueConfig.getIssueType(), issueConfig.getProjectName(), fieldsBuilder);
            logger.debug("Created new Jira Cloud issue: {}", issue.getKey());
            String issueKey = issue.getKey();
            addIssueProperties(issueKey, issueProperties);
            addComment(issueKey, "This issue was automatically created by Alert.");
            for (String additionalComment : contentModel.getAdditionalComments()) {
                String comment = String.format("%s \n %s", DESCRIPTION_CONTINUED_TEXT, additionalComment);
                addComment(issueKey, comment);
            }
            return issue;
        } catch (IntegrationRestException e) {
            throw improveRestException(e, issueCreator);
        }
    }

    @Override
    protected boolean transitionIssue(IssueResponseModel issueModel, IssueConfig issueConfig, OperationType operation) throws IntegrationException {
        return jiraTransitionHelper.transitionIssueIfNecessary(issueModel.getKey(), issueConfig, operation);
    }

    private IssueTrackerException improveRestException(IntegrationRestException restException, String issueCreatorEmail) {
        JsonObject responseContent = gson.fromJson(restException.getHttpResponseContent(), JsonObject.class);
        List<String> responseErrors = new ArrayList<>();
        if (null != responseContent) {
            JsonObject errors = responseContent.get("errors").getAsJsonObject();
            JsonElement reporterErrorMessage = errors.get("reporter");
            if (null != reporterErrorMessage) {
                return IssueTrackerFieldException.singleFieldError(
                    getIssueCreatorFieldKey(), String.format("There was a problem assigning '%s' to the issue. Please ensure that the user is assigned to the project and has permission to transition issues.", issueCreatorEmail)
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

        return new IssueTrackerException(message, restException);
    }

    private void addIssueProperties(String issueKey, IssueProperties issueProperties) throws IntegrationException {
        jiraIssuePropertyHelper.addPropertiesToIssue(issueKey, issueProperties);
    }

    private IssueRequestModelFieldsBuilder createFieldsBuilder(IssueContentModel contentModel) {
        IssueRequestModelFieldsBuilder fieldsBuilder = new IssueRequestModelFieldsBuilder();
        fieldsBuilder.setSummary(contentModel.getTitle());
        fieldsBuilder.setDescription(contentModel.getDescription());

        return fieldsBuilder;
    }
}
