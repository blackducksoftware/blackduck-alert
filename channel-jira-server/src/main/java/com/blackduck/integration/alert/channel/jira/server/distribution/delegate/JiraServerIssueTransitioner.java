package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueTransitioner;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;
import com.blackduck.integration.jira.common.model.request.IssueRequestModel;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueService;

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
