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
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannel;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.channel.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueCreatorTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TestIssueRequestCreator;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;

public class JiraCloudCreateIssueTestAction extends IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudCreateIssueTestAction.class);
    private final Gson gson;

    public JiraCloudCreateIssueTestAction(JiraCloudChannel jiraCloudChannel, Gson gson, TestIssueRequestCreator testIssueRequestCreator) {
        super(jiraCloudChannel, testIssueRequestCreator);
        this.gson = gson;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return JiraCloudProperties.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraCloudProperties.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getTodoStatusFieldKey() {
        return JiraTransitionHandler.TODO_STATUS_CATEGORY_KEY;
    }

    @Override
    protected String getDoneStatusFieldKey() {
        return JiraTransitionHandler.DONE_STATUS_CATEGORY_KEY;
    }

    @Override
    protected TransitionHandler<TransitionComponent> createTransitionHandler(IssueTrackerContext context) throws IntegrationException {
        JiraCloudProperties jiraProperties = createJiraProperties(context);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        return new JiraCloudTransitionHandler(issueService);
    }

    @Override
    protected void safelyCleanUpIssue(IssueTrackerContext context, String issueKey) {
        try {
            JiraCloudProperties jiraProperties = createJiraProperties(context);
            JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            IssueService issueService = jiraCloudServiceFactory.createIssueService();
            issueService.deleteIssue(issueKey);
        } catch (IntegrationException e) {
            logger.warn(String.format("There was a problem trying to delete the Jira Cloud distribution test issue, '%s': %s", issueKey, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
    }

    private JiraCloudProperties createJiraProperties(IssueTrackerContext context) {
        return (JiraCloudProperties) context.getIssueTrackerConfig();
    }

}
