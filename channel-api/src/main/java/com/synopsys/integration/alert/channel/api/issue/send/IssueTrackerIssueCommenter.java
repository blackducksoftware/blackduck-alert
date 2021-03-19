/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.io.Serializable;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.exception.AlertException;

public abstract class IssueTrackerIssueCommenter<T extends Serializable> {
    public static final String COMMENTING_DISABLED_MESSAGE = "Commenting on issues is disabled. Skipping.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerIssueResponseCreator issueResponseCreator;

    protected IssueTrackerIssueCommenter(IssueTrackerIssueResponseCreator issueResponseCreator) {
        this.issueResponseCreator = issueResponseCreator;
    }

    public final Optional<IssueTrackerIssueResponseModel<T>> commentOnIssue(IssueCommentModel<T> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return Optional.empty();
        }

        addComments(issueCommentModel);
        IssueTrackerIssueResponseModel<T> responseModel = issueResponseCreator.createIssueResponse(issueCommentModel.getSource(), issueCommentModel.getExistingIssueDetails(), IssueOperation.UPDATE);
        return Optional.of(responseModel);
    }

    protected abstract boolean isCommentingEnabled();

    protected abstract void addComment(String comment, ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel source) throws AlertException;

    protected void addComments(IssueCommentModel<T> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : issueCommentModel.getComments()) {
            addComment(comment, issueCommentModel.getExistingIssueDetails(), issueCommentModel.getSource());
        }
    }

}
