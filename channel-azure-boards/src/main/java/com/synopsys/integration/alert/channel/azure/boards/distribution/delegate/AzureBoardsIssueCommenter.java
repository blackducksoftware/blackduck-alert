/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.delegate;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.comment.AzureWorkItemCommentService;

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
