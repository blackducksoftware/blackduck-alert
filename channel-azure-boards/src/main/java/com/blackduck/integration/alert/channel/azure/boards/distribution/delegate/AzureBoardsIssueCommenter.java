/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.delegate;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueCommenter;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.service.comment.AzureWorkItemCommentService;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

public class AzureBoardsIssueCommenter extends IssueTrackerIssueCommenter<Integer> {
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemCommentService commentService;

    public AzureBoardsIssueCommenter(
        IssueTrackerIssueResponseCreator issueResponseCreator,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemCommentService commentService
    ) {
        super(issueResponseCreator);
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.commentService = commentService;
    }

    @Override
    protected boolean isCommentingEnabled() {
        return distributionDetails.isAddComments();
    }

    @Override
    protected void addComment(String comment, ExistingIssueDetails<Integer> existingIssueDetails, @Nullable ProjectIssueModel source) throws AlertException {
        try {
            commentService.addComment(organizationName, distributionDetails.getProjectNameOrId(), existingIssueDetails.getIssueId(), comment);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("Failed to add Azure Boards comment. Issue ID: %s", existingIssueDetails.getIssueId()), e);
        }
    }

}
