/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.common.delegate;

import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingConsumer;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;

public abstract class JiraIssueCommenter extends IssueTrackerIssueCommenter<String> {
    private final ThrowingConsumer<IssueCommentRequestModel, IntegrationException> addCommentConsumer;

    protected JiraIssueCommenter(IssueTrackerIssueResponseCreator<String> issueResponseCreator, ThrowingConsumer<IssueCommentRequestModel, IntegrationException> addCommentConsumer) {
        super(issueResponseCreator);
        this.addCommentConsumer = addCommentConsumer;
    }

    @Override
    protected final void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, ProjectIssueModel source) throws AlertException {
        try {
            IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(existingIssueDetails.getIssueKey(), comment);
            addCommentConsumer.accept(issueCommentRequestModel);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to add a comment in Jira. Issue Key: %s", existingIssueDetails.getIssueKey()), e);
        }
    }

}
