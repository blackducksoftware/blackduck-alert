/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannel;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.channel.jira.common.util.JiraTransitionHandler;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
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
        return JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    protected String getResolveTransitionFieldKey() {
        return JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
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
