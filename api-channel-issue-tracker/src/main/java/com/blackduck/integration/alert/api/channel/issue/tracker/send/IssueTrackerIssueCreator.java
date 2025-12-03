/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;

public abstract class IssueTrackerIssueCreator<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerChannelKey channelKey;
    private final IssueTrackerIssueCommenter<T> commenter;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;

    protected IssueTrackerIssueCreator(
        IssueTrackerChannelKey channelKey,
        IssueTrackerIssueCommenter<T> commenter,
        IssueTrackerCallbackInfoCreator callbackInfoCreator
    ) {
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
    protected Optional<String> getAlertSearchKeys(ExistingIssueDetails<T> existingIssueDetails, @Nullable ProjectIssueModel alertIssueSource) {
        return Optional.empty();
    }

    private void addPostCreateComments(ExistingIssueDetails<T> issueDetails, IssueCreationModel creationModel, @Nullable ProjectIssueModel projectSource) throws AlertException {
        LinkedList<String> postCreateComments = new LinkedList<>();
        Optional<String> searchKeys = getAlertSearchKeys(issueDetails, projectSource);
        postCreateComments.add("This issue was automatically created by Alert.");
        // if the issue tracker creates a comment with search keys then add it here.
        searchKeys.ifPresent(postCreateComments::add);
        postCreateComments.addAll(creationModel.getPostCreateComments());

        IssueCommentModel<T> commentRequestModel = new IssueCommentModel<>(issueDetails, postCreateComments, projectSource);
        commenter.commentOnIssue(commentRequestModel);
    }

}
