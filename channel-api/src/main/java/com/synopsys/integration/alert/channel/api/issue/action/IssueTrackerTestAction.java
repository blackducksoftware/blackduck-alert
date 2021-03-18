/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.action;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSenderFactory;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;

public abstract class IssueTrackerTestAction<D extends DistributionJobDetailsModel, T extends Serializable> implements ChannelDistributionTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerMessageSenderFactory<D, T> messageSenderFactory;

    public IssueTrackerTestAction(IssueTrackerMessageSenderFactory<D, T> messageSenderFactory) {
        this.messageSenderFactory = messageSenderFactory;
    }

    @Override
    public MessageResult testConfig(DistributionJobModel testJobModel, @Nullable String customTopic, @Nullable String customMessage, @Nullable String destination) throws AlertException {
        D distributionDetails = (D) testJobModel.getDistributionJobDetails();
        IssueTrackerMessageSender<T> messageSender = messageSenderFactory.createMessageSender(distributionDetails);

        String topicString = Optional.ofNullable(customTopic).orElse("Alert Test Topic");
        String messageString = Optional.ofNullable(customMessage).orElse("Alert Test Message");

        // TODO determine if source should be required everywhere
        ProjectIssueModel testProjectIssueModel = createPlaceholderProjectIssueModel(testJobModel.getBlackDuckGlobalConfigId());

        String postCreateComment = String.format("Created by [ Test Configuration ] in the Alert Distribution Job: %s", testJobModel.getName());
        IssueCreationModel creationRequest = IssueCreationModel.simple(topicString, messageString, List.of(postCreateComment), testProjectIssueModel.getProvider());
        IssueTrackerModelHolder<T> creationRequestModelHolder = new IssueTrackerModelHolder<>(List.of(creationRequest), List.of(), List.of());

        List<IssueTrackerIssueResponseModel<T>> createdIssues;
        try {
            createdIssues = messageSender.sendMessages(creationRequestModelHolder);
        } catch (AlertException e) {
            logger.debug("Failed to create test issue", e);
            return new MessageResult("Failed to create issue: " + e.getMessage());
        }

        int createdIssuesSize = createdIssues.size();
        if (createdIssuesSize != 1) {
            return new MessageResult(String.format("Expected [1] issue to be created, but there were actually [%d]", createdIssuesSize));
        }

        if (!hasResolveTransition(distributionDetails)) {
            return MessageResult.success();
        }

        IssueTrackerIssueResponseModel<T> createdIssue = createdIssues.get(0);
        ExistingIssueDetails<T> existingIssueDetails = new ExistingIssueDetails<>(createdIssue.getIssueId(), createdIssue.getIssueKey(), createdIssue.getIssueTitle(), createdIssue.getIssueLink());

        Optional<MessageResult> optionalResolveFailure = transitionTestIssueOrReturnFailureResult(messageSender, IssueOperation.RESOLVE, existingIssueDetails, testProjectIssueModel);
        if (optionalResolveFailure.isPresent()) {
            return optionalResolveFailure.get();
        }

        if (!hasReopenTransition(distributionDetails)) {
            return MessageResult.success();
        }

        Optional<MessageResult> optionalReopenFailure = transitionTestIssueOrReturnFailureResult(messageSender, IssueOperation.OPEN, existingIssueDetails, testProjectIssueModel);
        if (optionalReopenFailure.isPresent()) {
            return optionalReopenFailure.get();
        }

        return transitionTestIssueOrReturnFailureResult(messageSender, IssueOperation.RESOLVE, existingIssueDetails, testProjectIssueModel).orElse(MessageResult.success());
    }

    private Optional<MessageResult> transitionTestIssueOrReturnFailureResult(IssueTrackerMessageSender<T> messageSender, IssueOperation operation, ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel testProjectIssueModel) {
        String postTransitionComment = String.format("Successfully tested the %s operation", operation.name());
        IssueTransitionModel<T> resolveRequest = new IssueTransitionModel<>(existingIssueDetails, operation, List.of(postTransitionComment), testProjectIssueModel);
        IssueTrackerModelHolder<T> resolveRequestModelHolder = new IssueTrackerModelHolder<>(List.of(), List.of(resolveRequest), List.of());

        List<IssueTrackerIssueResponseModel<T>> transitionedIssues;
        try {
            transitionedIssues = messageSender.sendMessages(resolveRequestModelHolder);
        } catch (IssueMissingTransitionException e) {
            logger.debug("Failed to transition test issue", e);
            String validTransitions = StringUtils.join(e.getValidTransitions(), ", ");
            return Optional.of(new MessageResult(String.format("Invalid transition: %s. Please choose a valid transition: %s", e.getMissingTransition(), validTransitions)));
        } catch (AlertException e) {
            logger.debug("Failed to transition test issue", e);
            return Optional.of(new MessageResult(String.format("Failed to perform %s transition: %s", operation.name(), e.getMessage())));
        }

        int transitionedIssuesSize = transitionedIssues.size();
        if (transitionedIssuesSize != 1) {
            return Optional.of(new MessageResult(String.format("Expected [1] issue to be transitioned, but there were actually [%d]", transitionedIssuesSize)));
        }
        return Optional.empty();
    }

    protected abstract boolean hasResolveTransition(D distributionDetails);

    protected abstract boolean hasReopenTransition(D distributionDetails);

    private ProjectIssueModel createPlaceholderProjectIssueModel(Long blackDuckConfigId) {
        LinkableItem providerItem = new LinkableItem("Provider Test Label", "Provider Config Test Name");
        ProviderDetails providerDetails = new ProviderDetails(blackDuckConfigId, providerItem);

        LinkableItem projectItem = new LinkableItem("Project Test Label", "Project Test Name");
        LinkableItem projectVersionItem = new LinkableItem("Project-Version Test Label", "Project-Version Test Name");

        LinkableItem componentItem = new LinkableItem("Component Test Label", "Component Test Value");
        IssueBomComponentDetails bomComponentDetails = IssueBomComponentDetails.fromSearchResults(componentItem, null);

        return ProjectIssueModel.bom(
            providerDetails,
            projectItem,
            projectVersionItem,
            bomComponentDetails
        );
    }

}
