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
package com.synopsys.integration.alert.issuetracker.jira.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.issuetracker.IssueCreator;
import com.synopsys.integration.alert.issuetracker.IssueCreatorTestAction;
import com.synopsys.integration.alert.issuetracker.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.IssueTrackerService;
import com.synopsys.integration.alert.issuetracker.TransitionValidator;
import com.synopsys.integration.alert.issuetracker.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.issuetracker.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;

public class JiraCloudCreateIssueTestAction extends IssueCreatorTestAction {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudCreateIssueTestAction.class);
    private Gson gson;

    public JiraCloudCreateIssueTestAction(IssueTrackerService issueTrackerService, Gson gson, IssueCreator issueCreator) {
        super(issueTrackerService, issueCreator);
        this.gson = gson;
    }

    @Override
    protected String getOpenTransitionFieldKey() {
        return JiraProperties.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraProperties.KEY_RESOLVE_WORKFLOW_TRANSITION;
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
    protected TransitionValidator<TransitionComponent> createTransitionValidator(IssueTrackerContext context) throws IntegrationException {
        JiraProperties jiraProperties = createJiraProperties(context);
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        return new JiraCloudTransitionHandler(issueService);
    }

    @Override
    protected void safelyCleanUpIssue(IssueTrackerContext context, String issueKey) {
        try {
            JiraProperties jiraProperties = createJiraProperties(context);
            JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            IssueService issueService = jiraCloudServiceFactory.createIssueService();
            issueService.deleteIssue(issueKey);
        } catch (IntegrationException e) {
            logger.warn("There was a problem trying to delete a the Jira Cloud distribution test issue, {}: {}", issueKey, e);
        }
    }

    private JiraProperties createJiraProperties(IssueTrackerContext context) {
        return (JiraProperties) context.getIssueTrackerConfig();
    }
}
