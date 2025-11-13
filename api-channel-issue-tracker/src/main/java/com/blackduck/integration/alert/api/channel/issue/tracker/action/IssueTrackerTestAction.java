/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.action;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSenderFactory;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.common.channel.DistributionChannelTestAction;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.exception.AlertFieldException;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class IssueTrackerTestAction<D extends DistributionJobDetailsModel, T extends Serializable> extends DistributionChannelTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueTrackerMessageSenderFactory<D, T, IssueTrackerModelHolder<T>> messageSenderFactory;

    protected IssueTrackerTestAction(IssueTrackerChannelKey issueTrackerChannelKey, IssueTrackerMessageSenderFactory<D, T, IssueTrackerModelHolder<T>> messageSenderFactory) {
        super(issueTrackerChannelKey);
        this.messageSenderFactory = messageSenderFactory;
    }

    @Override
    public MessageResult testConfig(DistributionJobModel testJobModel, String jobName, @Nullable String customTopic, @Nullable String customMessage) throws AlertException {
        D distributionDetails = (D) testJobModel.getDistributionJobDetails();
        IssueTrackerMessageSender<T> messageSender = messageSenderFactory.createMessageSender(distributionDetails, testJobModel.getChannelGlobalConfigId());

        String topicString = Optional.ofNullable(customTopic).orElse("Alert Test Topic");
        String messageString = Optional.ofNullable(customMessage).orElse("Alert Test Message");

        // TODO determine if source should be required everywhere
        ProjectIssueModel testProjectIssueModel = createPlaceholderProjectIssueModel(testJobModel.getBlackDuckGlobalConfigId());

        String postCreateComment = String.format("Created by [ Test Configuration ] in the Alert Distribution Job: %s", testJobModel.getName());
        IssueCreationModel creationRequest = IssueCreationModel.simple(topicString, messageString, List.of(postCreateComment), testProjectIssueModel.getProvider());
        IssueTrackerModelHolder<T> creationRequestModelHolder = new IssueTrackerModelHolder<>(List.of(creationRequest), List.of(), List.of());

        IssueTrackerTestActionFieldStatusCreator fieldStatusCreator = new IssueTrackerTestActionFieldStatusCreator();

        List<IssueTrackerIssueResponseModel<T>> createdIssues;
        try {
            createdIssues = messageSender.sendMessages(creationRequestModelHolder);
        } catch (AlertFieldException e) {
            logger.error("Failed to create test issue", e);
            return new MessageResult("Failed to create issue: " + e.getMessage(), e.getFieldErrors());
        } catch (AlertException e) {
            logger.error("Failed to create test issue", e);
            return new MessageResult("Failed to create issue: " + e.getMessage(), fieldStatusCreator.createWithoutField(e.getMessage()));
        }

        int createdIssuesSize = createdIssues.size();
        if (createdIssuesSize != 1) {
            String errorMessage = String.format("Expected [1] issue to be created, but there were actually [%d]", createdIssuesSize);
            return new MessageResult(errorMessage, fieldStatusCreator.createWithoutField(errorMessage));
        }

        IssueTrackerIssueResponseModel<T> createdIssue = createdIssues.get(0);
        ExistingIssueDetails<T> existingIssueDetails = new ExistingIssueDetails<>(
            createdIssue.getIssueId(),
            createdIssue.getIssueKey(),
            createdIssue.getIssueTitle(),
            createdIssue.getIssueLink(),
            IssueStatus.RESOLVABLE,
            IssueCategory.BOM
        );

        if (!hasResolveTransition(distributionDetails)) {
            return createSuccessMessageResult(existingIssueDetails);
        }

        IssueTrackerTransitionTestAction<T> transitionTestAction = new IssueTrackerTransitionTestAction<>(messageSender, fieldStatusCreator);

        Optional<MessageResult> optionalResolveFailure = transitionTestAction.transitionTestIssueOrReturnFailureResult(
            IssueOperation.RESOLVE,
            existingIssueDetails,
            testProjectIssueModel
        );
        if (optionalResolveFailure.isPresent()) {
            return optionalResolveFailure.get();
        }

        if (!hasReopenTransition(distributionDetails)) {
            return createSuccessMessageResult(existingIssueDetails);
        }

        return transitionTestAction.transitionTestIssueOrReturnFailureResult(IssueOperation.OPEN, existingIssueDetails, testProjectIssueModel)
            .orElseGet(() -> transitionTestAction.transitionTestIssueOrReturnFailureResult(IssueOperation.RESOLVE, existingIssueDetails, testProjectIssueModel)
                .orElse(createSuccessMessageResult(existingIssueDetails)));
    }

    protected abstract boolean hasResolveTransition(D distributionDetails);

    protected abstract boolean hasReopenTransition(D distributionDetails);

    private MessageResult createSuccessMessageResult(ExistingIssueDetails<T> issueDetails) {
        return new MessageResult(String.format("Success: %s", issueDetails.getIssueUILink()));
    }

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
