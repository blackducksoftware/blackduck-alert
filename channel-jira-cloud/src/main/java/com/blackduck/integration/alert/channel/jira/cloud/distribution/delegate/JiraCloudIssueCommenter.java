/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCommenter;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.model.IssueCommentRequestModel;

public class JiraCloudIssueCommenter extends JiraIssueCommenter<IssueCommentRequestModel> {
    private final IssueService issueService;
    private final JiraCloudJobDetailsModel distributionDetails;

    public JiraCloudIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, IssueService issueService, JiraCloudJobDetailsModel distributionDetails) {
        super(issueResponseCreator);
        this.issueService = issueService;
        this.distributionDetails = distributionDetails;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return true;
    }

    @Override
    protected void addComment(IssueCommentRequestModel requestModel) throws IntegrationException {
        issueService.addComment(requestModel);
    }

    @Override
    protected IssueCommentRequestModel createCommentModel(String comment, ExistingIssueDetails<String> existingIssueDetails) throws IntegrationException {
        return IssueCommentRequestModel.commentForIssue(existingIssueDetails.getIssueKey(), comment);
    }
}
