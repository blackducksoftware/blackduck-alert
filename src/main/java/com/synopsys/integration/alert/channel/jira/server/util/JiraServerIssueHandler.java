/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira.server.util;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraMessageParser;
import com.synopsys.integration.alert.channel.jira.common.util.JiraIssueHandler;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueHandler extends JiraIssueHandler {
    private static final Logger logger = LoggerFactory.getLogger(JiraServerIssueHandler.class);

    private final IssueService issueService;
    private final JiraServerProperties jiraProperties;

    private final JiraServerIssuePropertyHandler jiraIssuePropertyHelper;

    public JiraServerIssueHandler(IssueService issueService, JiraServerProperties jiraProperties, JiraMessageParser jiraMessageParser, Gson gson, JiraServerTransitionHandler jiraTransitionHandler,
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler) {
        super(jiraMessageParser, gson, jiraTransitionHandler, jiraIssuePropertyHandler);
        this.issueService = issueService;
        this.jiraProperties = jiraProperties;
        this.jiraIssuePropertyHelper = jiraIssuePropertyHandler;
    }

    @Override
    public IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException {
        return issueService.createIssue(new IssueCreationRequestModel(issueCreator, issueType, projectName, fieldsBuilder));
    }

    @Override
    protected List<IssueResponseModel> retrieveExistingIssues(String projectKey, String provider, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem componentItem, String alertIssueUniqueId)
        throws IntegrationException {
        List<IssueSearchIssueComponent> searchIssueModels = jiraIssuePropertyHelper
                                                                .findIssues(projectKey, provider, providerUrl, topic, nullableSubTopic, componentItem, alertIssueUniqueId)
                                                                .map(IssueSearchResponseModel::getIssues)
                                                                .orElse(List.of());
        List<IssueResponseModel> issues = new LinkedList<>();

        for (IssueSearchIssueComponent searchIssue : searchIssueModels) {
            try {
                issues.add(issueService.getIssue(searchIssue.getKey()));
            } catch (IntegrationException ex) {
                logger.error("Error getting issue details for issue. {}", searchIssue.getKey());
                logger.debug("Cause", ex);
            }
        }

        return issues;
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
