/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.delegate;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCommenter;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.request.IssueCommentRequestModel;

public abstract class JiraIssueCommenter extends IssueTrackerIssueCommenter<String> {

    protected JiraIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator) {
        super(issueResponseCreator);
    }

    @Override
    protected final void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, @Nullable ProjectIssueModel source) throws AlertException {
        try {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(existingIssueDetails.getIssueKey(), comment);
            addComment(issueCommentRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", existingIssueDetails.getIssueKey()), e);
        }
    }

    protected abstract void addComment(IssueCommentRequestModel requestModel) throws IntegrationException;

}
