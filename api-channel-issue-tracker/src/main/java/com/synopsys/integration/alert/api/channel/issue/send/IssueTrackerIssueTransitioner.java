/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;

public abstract class IssueTrackerIssueTransitioner<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerIssueCommenter<T> commenter;
    private final IssueTrackerIssueResponseCreator issueResponseCreator;

    protected IssueTrackerIssueTransitioner(IssueTrackerIssueCommenter<T> commenter, IssueTrackerIssueResponseCreator issueResponseCreator) {
        this.commenter = commenter;
        this.issueResponseCreator = issueResponseCreator;
    }

    public final Optional<IssueTrackerIssueResponseModel<T>> transitionIssue(IssueTransitionModel<T> issueTransitionModel) throws AlertException {
        IssueOperation issueOperation = issueTransitionModel.getIssueOperation();
        ExistingIssueDetails<T> existingIssueDetails = issueTransitionModel.getExistingIssueDetails();

        Optional<IssueTrackerIssueResponseModel<T>> transitionResponse = Optional.empty();

        Optional<String> optionalTransitionName = retrieveJobTransitionName(issueOperation);
        if (optionalTransitionName.isPresent()) {
            String transitionName = optionalTransitionName.get();

            boolean shouldAttemptTransition = isTransitionRequired(existingIssueDetails, issueOperation);
            if (shouldAttemptTransition) {
                attemptTransition(issueOperation, existingIssueDetails, transitionName);
                IssueTrackerIssueResponseModel<T> transitionResponseModel = issueResponseCreator.createIssueResponse(issueTransitionModel.getSource(), existingIssueDetails, issueOperation);
                transitionResponse = Optional.of(transitionResponseModel);
            } else {
                logger.debug("The issue is already in the status category that would result from this transition ({}). Issue Details: {}", transitionName, existingIssueDetails);
            }
        } else {
            logger.debug("No transition name was provided so no '{}' transition will be performed. Issue Details: {}", issueOperation.name(), existingIssueDetails);
        }

        IssueCommentModel<T> commentRequestModel = new IssueCommentModel<>(existingIssueDetails, issueTransitionModel.getPostTransitionComments(), issueTransitionModel.getSource());
        commenter.commentOnIssue(commentRequestModel);

        return transitionResponse;
    }

    protected abstract Optional<String> retrieveJobTransitionName(IssueOperation issueOperation);

    protected abstract boolean isTransitionRequired(ExistingIssueDetails<T> existingIssueDetails, IssueOperation issueOperation) throws AlertException;

    protected abstract void findAndPerformTransition(ExistingIssueDetails<T> existingIssueDetails, String transitionName) throws AlertException, IssueMissingTransitionException;

    private void attemptTransition(IssueOperation issueOperation, ExistingIssueDetails<T> existingIssueDetails, String transitionName) throws AlertException {
        try {
            findAndPerformTransition(existingIssueDetails, transitionName);
        } catch (IssueMissingTransitionException e) {
            addTransitionFailureComment(issueOperation, existingIssueDetails, e);
            throw e;
        }
    }

    private void addTransitionFailureComment(IssueOperation issueOperation, ExistingIssueDetails<T> existingIssueDetails, IssueMissingTransitionException issueMissingTransitionException) {
        String failureComment = String.format("The %s operation was performed on this component in BlackDuck, but Alert failed to transition the issue: %s", issueOperation.name(), issueMissingTransitionException.getMessage());
        IssueCommentModel<T> failureCommentRequestModel = new IssueCommentModel<>(existingIssueDetails, List.of(failureComment), null);
        try {
            commenter.commentOnIssue(failureCommentRequestModel);
        } catch (AlertException e) {
            logger.warn("Failed to add comment for {}", IssueMissingTransitionException.class.getSimpleName(), e);
        }
    }

}
