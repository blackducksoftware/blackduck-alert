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
package com.synopsys.integration.alert.channel.jira.cloud.util;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraProperties;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.common.util.JiraIssueHandler;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

public class JiraCloudIssueHandler extends JiraIssueHandler {
    private final IssueService issueService;
    private final JiraProperties jiraProperties;
    private final JiraCloudIssuePropertyHandler jiraIssuePropertyHandler;

    public JiraCloudIssueHandler(IssueService issueService, JiraProperties jiraProperties, JiraMessageParser jiraMessageParser, Gson gson, JiraCloudTransitionHandler jiraTransitionHandler,
        JiraCloudIssuePropertyHandler jiraIssuePropertyHandler) {
        super(jiraMessageParser, gson, jiraTransitionHandler, jiraIssuePropertyHandler);
        this.issueService = issueService;
        this.jiraProperties = jiraProperties;
        this.jiraIssuePropertyHandler = jiraIssuePropertyHandler;
    }

    @Override
    public IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException {
        return issueService.createIssue(new IssueCreationRequestModel(issueCreator, issueType, projectName, fieldsBuilder, List.of()));
    }

    @Override
    protected List<IssueResponseModel> retrieveExistingIssues(String projectKey, String provider, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem componentItem, String alertIssueUniqueId)
        throws IntegrationException {
        return jiraIssuePropertyHandler
                   .findIssues(projectKey, provider, providerUrl, topic, nullableSubTopic, componentItem, alertIssueUniqueId)
                   .map(IssueSearchResponseModel::getIssues)
                   .orElse(List.of());
    }

    @Override
    protected void addComment(String issueKey, String comment) throws IntegrationException {
        IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
        issueService.addComment(issueCommentRequestModel);
    }

    @Override
    protected String getIssueKey(IssueResponseModel issueModel) {
        return issueModel.getKey();
    }

    @Override
    protected String getIssueTrackerUrl() {
        return jiraProperties.getUrl();
    }

}
