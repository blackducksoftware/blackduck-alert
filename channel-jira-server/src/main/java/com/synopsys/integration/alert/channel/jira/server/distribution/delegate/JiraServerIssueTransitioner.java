/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.delegate.JiraIssueTransitioner;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueTransitioner extends JiraIssueTransitioner {
    private final IssueService issueService;

    public JiraServerIssueTransitioner(
        JiraServerIssueCommenter jiraServerIssueCommenter,
        IssueTrackerIssueResponseCreator issueResponseCreator,
        JiraServerJobDetailsModel distributionDetails,
        IssueService issueService
    ) {
        super(jiraServerIssueCommenter, issueResponseCreator, distributionDetails.getResolveTransition(), distributionDetails.getReopenTransition());
        this.issueService = issueService;
    }

    @Override
    protected StatusDetailsComponent fetchIssueStatus(String issueKey) throws IntegrationException {
        return issueService.getStatus(issueKey);
    }

    @Override
    protected TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException {
        return issueService.getTransitions(issueKey);
    }

    @Override
    protected void executeTransitionRequest(IssueRequestModel issueRequestModel) throws IntegrationException {
        issueService.transitionIssue(issueRequestModel);
    }

}
