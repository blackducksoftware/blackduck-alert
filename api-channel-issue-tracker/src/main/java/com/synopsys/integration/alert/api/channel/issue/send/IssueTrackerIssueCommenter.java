/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

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
        IssueTrackerIssueResponseModel<T> responseModel = issueResponseCreator.createIssueResponse(issueCommentModel.getSource().orElse(null), issueCommentModel.getExistingIssueDetails(), IssueOperation.UPDATE);
        return Optional.of(responseModel);
    }

    protected abstract boolean isCommentingEnabled();

    protected abstract void addComment(String comment, ExistingIssueDetails<T> existingIssueDetails, @Nullable ProjectIssueModel source) throws AlertException;

    protected void addComments(IssueCommentModel<T> issueCommentModel) throws AlertException {
        if (!isCommentingEnabled()) {
            logger.debug(COMMENTING_DISABLED_MESSAGE);
            return;
        }

        for (String comment : issueCommentModel.getComments()) {
            addComment(comment, issueCommentModel.getExistingIssueDetails(), issueCommentModel.getSource().orElse(null));
        }
    }

}
