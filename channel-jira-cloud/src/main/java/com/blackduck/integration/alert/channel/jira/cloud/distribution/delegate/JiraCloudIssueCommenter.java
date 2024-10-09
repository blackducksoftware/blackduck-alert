/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate;

import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCommenter;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.model.request.IssueCommentRequestModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

public class JiraCloudIssueCommenter extends JiraIssueCommenter {
    private final IssueService issueService;
    private final JiraCloudJobDetailsModel distributionDetails;

    public JiraCloudIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, IssueService issueService, JiraCloudJobDetailsModel distributionDetails) {
        super(issueResponseCreator);
        this.issueService = issueService;
        this.distributionDetails = distributionDetails;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

    @Override
    protected void addComment(IssueCommentRequestModel requestModel) throws IntegrationException {
        issueService.addComment(requestModel);
    }

}
