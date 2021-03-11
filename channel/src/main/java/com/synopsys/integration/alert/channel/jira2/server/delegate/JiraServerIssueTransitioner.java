/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.server.delegate;

import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.jira2.common.delegate.JiraIssueTransitioner;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.request.IssueRequestModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueTransitioner extends JiraIssueTransitioner {
    private final JiraServerJobDetailsModel distributionDetails;
    private final IssueService issueService;

    public JiraServerIssueTransitioner(
        JiraServerIssueCommenter jiraServerIssueCommenter,
        IssueTrackerIssueResponseCreator issueResponseCreator,
        JiraServerJobDetailsModel distributionDetails,
        IssueService issueService
    ) {
        super(jiraServerIssueCommenter, issueResponseCreator);
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
    }

    @Override
    protected String extractReopenTransitionName() {
        return distributionDetails.getReopenTransition();
    }

    @Override
    protected String extractResolveTransitionName() {
        return distributionDetails.getResolveTransition();
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
