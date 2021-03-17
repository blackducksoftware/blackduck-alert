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
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.exception.AlertException;

public abstract class IssueTrackerIssueTransitioner<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerIssueCommenter<T> commenter;
    private final IssueTrackerIssueResponseCreator issueResponseCreator;

    protected IssueTrackerIssueTransitioner(IssueTrackerIssueCommenter<T> commenter, IssueTrackerIssueResponseCreator issueResponseCreator) {
        this.commenter = commenter;
        this.issueResponseCreator = issueResponseCreator;
    }

    public final Optional<IssueTrackerIssueResponseModel> transitionIssue(IssueTransitionModel<T> issueTransitionModel) throws AlertException {
        IssueOperation issueOperation = issueTransitionModel.getIssueOperation();
        ExistingIssueDetails<T> existingIssueDetails = issueTransitionModel.getExistingIssueDetails();

        Optional<IssueTrackerIssueResponseModel> transitionResponse = Optional.empty();

        Optional<String> optionalTransitionName = retrieveJobTransitionName(issueOperation);
        if (optionalTransitionName.isPresent()) {
            String transitionName = optionalTransitionName.get();

            boolean shouldAttemptTransition = isTransitionRequired(existingIssueDetails, issueOperation);
            if (shouldAttemptTransition) {
                findAndPerformTransition(existingIssueDetails, transitionName);
                IssueTrackerIssueResponseModel transitionResponseModel = issueResponseCreator.createIssueResponse(issueTransitionModel.getSource(), existingIssueDetails, issueOperation);
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

    protected abstract void findAndPerformTransition(ExistingIssueDetails<T> existingIssueDetails, String transitionName) throws AlertException;

}
