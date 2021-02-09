/*
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
package com.synopsys.integration.alert.channel.jira.cloud.util;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
import com.synopsys.integration.alert.channel.jira.common.util.JiraIssueHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

public class JiraCloudIssueHandler extends JiraIssueHandler {
    private final IssueService issueService;
    private final JiraCloudProperties jiraProperties;
    private final JiraCloudIssuePropertyHandler jiraIssuePropertyHandler;

    public JiraCloudIssueHandler(IssueService issueService, JiraCloudProperties jiraProperties, Gson gson, JiraCloudTransitionHandler jiraTransitionHandler,
        JiraCloudIssuePropertyHandler jiraIssuePropertyHandler, JiraContentValidator jiraContentValidator) {
        super(gson, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator);
        this.issueService = issueService;
        this.jiraProperties = jiraProperties;
        this.jiraIssuePropertyHandler = jiraIssuePropertyHandler;
    }

    @Override
    public IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException {
        IssueCreationResponseModel issueCreationResponseModel = issueService.createIssue(new IssueCreationRequestModel(issueCreator, issueType, projectName, fieldsBuilder, Collections.emptyList()));
        return issueService.getIssue(issueCreationResponseModel.getKey());
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraCloudDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    protected List<IssueResponseModel> retrieveExistingIssues(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
        JiraIssueSearchProperties issueProperties = request.getIssueSearchProperties();
        return jiraIssuePropertyHandler
                   .findIssues(issueConfig.getProjectKey(), issueProperties)
                   .map(IssueSearchResponseModel::getIssues)
                   .orElse(Collections.emptyList());
    }

    @Override
    protected void addComment(IssueConfig issueConfig, String issueKey, String comment) throws IntegrationException {
        IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
        issueService.addComment(issueCommentRequestModel);
    }

    @Override
    protected String getIssueKey(IssueResponseModel issueModel) {
        return issueModel.getKey();
    }

    @Override
    protected IssueTrackerIssueResponseModel createResponseModel(AlertIssueOrigin alertIssueOrigin, String issueTitle, IssueOperation issueOperation, IssueResponseModel issueResponse) {
        String uiLink = JiraCallbackUtils.createUILink(issueResponse);
        return new IssueTrackerIssueResponseModel(alertIssueOrigin, issueResponse.getKey(), uiLink, issueTitle, issueOperation);
    }

    @Override
    protected String getIssueTrackerUrl() {
        return jiraProperties.getUrl();
    }

}
