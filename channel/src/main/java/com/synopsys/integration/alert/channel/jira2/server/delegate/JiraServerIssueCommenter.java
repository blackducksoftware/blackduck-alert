/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.server.delegate;

import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.jira2.common.delegate.JiraIssueCommenter;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueCommenter extends JiraIssueCommenter {
    private final JiraServerJobDetailsModel distributionDetails;

    public JiraServerIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, IssueService issueService, JiraServerJobDetailsModel distributionDetails) {
        super(issueResponseCreator, issueService::addComment);
        this.distributionDetails = distributionDetails;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

}
