/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.delegate.JiraIssueCommenter;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.request.IssueCommentRequestModel;
import com.blackduck.integration.jira.common.server.service.IssueService;

public class JiraServerIssueCommenter extends JiraIssueCommenter {
    private final IssueService issueService;
    private final JiraServerJobDetailsModel distributionDetails;

    public JiraServerIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, IssueService issueService, JiraServerJobDetailsModel distributionDetails) {
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
