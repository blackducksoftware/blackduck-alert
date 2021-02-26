/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.cloud.delegate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;

public class JiraCloudIssueCommenter extends IssueTrackerIssueCommenter<String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JiraCloudJobDetailsModel distributionDetails;
    private final IssueService issueService;

    public JiraCloudIssueCommenter(IssueTrackerIssueResponseCreator<String> issueResponseCreator, JiraCloudJobDetailsModel distributionDetails, IssueService issueService) {
        super(issueResponseCreator);
        this.distributionDetails = distributionDetails;
        this.issueService = issueService;
    }

    public void addComment(String issueKey, String comment) throws AlertException {
        addComments(issueKey, List.of(comment));
    }

    public void addComments(String issueKey, List<String> comments) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : comments) {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
            try {
                issueService.addComment(issueCommentRequestModel);
            } catch (IntegrationException e) {
                throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", issueKey), e);
            }
        }
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

    @Override
    protected void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, ProjectIssueModel source) throws AlertException {
        try {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(existingIssueDetails.getIssueKey(), comment);
            issueService.addComment(issueCommentRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", existingIssueDetails.getIssueKey()), e);
        }
    }

}
