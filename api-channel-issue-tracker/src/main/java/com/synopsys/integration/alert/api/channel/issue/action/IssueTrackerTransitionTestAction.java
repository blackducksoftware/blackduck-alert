/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.action;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.message.model.MessageResult;

public final class IssueTrackerTransitionTestAction<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerMessageSender<T> messageSender;
    private final IssueTrackerTestActionFieldStatusCreator fieldStatusCreator;

    public IssueTrackerTransitionTestAction(IssueTrackerMessageSender<T> messageSender, IssueTrackerTestActionFieldStatusCreator fieldStatusCreator) {
        this.messageSender = messageSender;
        this.fieldStatusCreator = fieldStatusCreator;
    }

    public Optional<MessageResult> transitionTestIssueOrReturnFailureResult(IssueOperation operation, ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel testProjectIssueModel) {
        String postTransitionComment = String.format("Successfully tested the %s operation", operation.name());
        IssueTransitionModel<T> resolveRequest = new IssueTransitionModel<>(existingIssueDetails, operation, List.of(postTransitionComment), testProjectIssueModel);
        IssueTrackerModelHolder<T> resolveRequestModelHolder = new IssueTrackerModelHolder<>(List.of(), List.of(resolveRequest), List.of());

        List<IssueTrackerIssueResponseModel<T>> transitionedIssues;
        try {
            transitionedIssues = messageSender.sendMessages(resolveRequestModelHolder);
        } catch (IssueMissingTransitionException e) {
            MessageResult issueMissingResult = handleIssueMessingTransitionException(e);
            return Optional.of(issueMissingResult);
        } catch (AlertException e) {
            MessageResult alertExceptionResult = handleAlertException(e, operation);
            return Optional.of(alertExceptionResult);
        }

        int transitionedIssuesSize = transitionedIssues.size();
        if (transitionedIssuesSize != 1) {
            String errorMessage = String.format("Expected [1] issue to be transitioned, but there were actually [%d]", transitionedIssuesSize);
            return Optional.of(new MessageResult(errorMessage, fieldStatusCreator.createWithoutField(errorMessage)));
        }
        return Optional.empty();
    }

    private MessageResult handleIssueMessingTransitionException(IssueMissingTransitionException e) {
        logger.debug("Failed to transition test issue", e);
        String validTransitions = StringUtils.join(e.getValidTransitions(), ", ");
        String errorMessage = String.format("Invalid transition: %s. Please choose a valid transition: %s", e.getMissingTransition(), validTransitions);
        return new MessageResult(errorMessage, fieldStatusCreator.createWithoutField(errorMessage));
    }

    private MessageResult handleAlertException(AlertException e, IssueOperation operation) {
        logger.debug("Failed to transition test issue", e);
        String errorMessage = String.format("Failed to perform %s transition: %s", operation.name(), e.getMessage());
        return new MessageResult(errorMessage, fieldStatusCreator.createWithoutField(errorMessage));
    }

}
