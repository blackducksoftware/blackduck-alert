/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueTransitioner;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;
import com.blackduck.integration.jira.common.model.request.IssueRequestModel;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraCloudIssueTransitioner extends JiraIssueTransitioner {
    private final IssueService issueService;

    public JiraCloudIssueTransitioner(
        JiraCloudIssueCommenter jiraCloudIssueCommenter,
        IssueTrackerIssueResponseCreator issueResponseCreator,
        JiraCloudJobDetailsModel distributionDetails,
        IssueService issueService
    ) {
        super(jiraCloudIssueCommenter, issueResponseCreator, distributionDetails.getResolveTransition(), distributionDetails.getReopenTransition());
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
