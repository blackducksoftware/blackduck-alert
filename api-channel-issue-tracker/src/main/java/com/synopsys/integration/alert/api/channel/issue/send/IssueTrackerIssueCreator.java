/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

public abstract class IssueTrackerIssueCreator<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerChannelKey channelKey;
    private final IssueTrackerIssueCommenter<T> commenter;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;

    protected IssueTrackerIssueCreator(
        IssueTrackerChannelKey channelKey,
        IssueTrackerIssueCommenter<T> commenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator) {
        this.channelKey = channelKey;
        this.commenter = commenter;
        this.callbackInfoCreator = callbackInfoCreator;
    }

    /**
     * This method does three things:<br />
     * 1. Creates a new issue in an issue-tracker.<br />
     * 2. If applicable, Assigns "Alert Search Properties" so that Alert can find the issue in the future.<br />
     * 3. Adds comments to the issue with any additional contextual information that the issue requires.<br />
     * @param alertIssueCreationModel - A model containing common issue-tracker fields, post-create comments, and details about the source of the model.
     * @return {@link IssueTrackerIssueResponseModel}
     * @throws AlertException Thrown if there is a problem connecting to the issue-tracker or if the issue-tracker server responds with an error.
     */
    public final IssueTrackerIssueResponseModel<T> createIssueTrackerIssue(IssueCreationModel alertIssueCreationModel) throws AlertException {
        ExistingIssueDetails<T> createdIssueDetails = createIssueAndExtractDetails(alertIssueCreationModel);
        logger.debug("Created new {} issue: {}", channelKey.getDisplayName(), createdIssueDetails);

        IssueTrackerCallbackInfo callbackInfo = null;
        Optional<ProjectIssueModel> optionalSource = alertIssueCreationModel.getSource();
        if (optionalSource.isPresent()) {
            ProjectIssueModel alertIssueSource = optionalSource.get();
            assignAlertSearchProperties(createdIssueDetails, alertIssueSource);
            callbackInfo = callbackInfoCreator.createCallbackInfo(alertIssueSource).orElse(null);
        }
        addPostCreateComments(createdIssueDetails, alertIssueCreationModel, optionalSource.orElse(null));

        return new IssueTrackerIssueResponseModel<>(
            createdIssueDetails.getIssueId(),
            createdIssueDetails.getIssueKey(),
            createdIssueDetails.getIssueUILink(),
            createdIssueDetails.getIssueSummary(),
            IssueOperation.OPEN,
            callbackInfo
        );
    }

    protected abstract ExistingIssueDetails<T> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) throws AlertException;

    protected abstract void assignAlertSearchProperties(ExistingIssueDetails<T> createdIssueDetails, ProjectIssueModel alertIssueSource) throws AlertException;

    private void addPostCreateComments(ExistingIssueDetails<T> issueDetails, IssueCreationModel creationModel, @Nullable ProjectIssueModel projectSource) throws AlertException {
        LinkedList<String> postCreateComments = new LinkedList<>(creationModel.getPostCreateComments());
        postCreateComments.addFirst("This issue was automatically created by Alert.");

        IssueCommentModel<T> commentRequestModel = new IssueCommentModel<>(issueDetails, postCreateComments, projectSource);
        commenter.commentOnIssue(commentRequestModel);
    }

}
