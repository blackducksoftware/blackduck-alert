/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;

public class IssueTrackerProcessor<T extends Serializable> implements IssueTrackerMessageProcessor<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<T>> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<T>> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    @Override
    public final IssueTrackerResponse<T> processMessages(ProviderMessageHolder messages, String jobName) throws AlertException {
        List<IssueTrackerModelHolder<T>> issueTrackerModels = new LinkedList<>();
        IssueTrackerModelHolder<T> simpleMessageHolder = modelExtractor.extractSimpleMessageIssueModels(messages.getSimpleMessages(), jobName);
        issueTrackerModels.add(simpleMessageHolder);
        logIssueTrackerMessages("Simple", simpleMessageHolder);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<T> projectMessageHolder = modelExtractor.extractProjectMessageIssueModels(projectMessage, jobName);
            issueTrackerModels.add(projectMessageHolder);
            logIssueTrackerMessages("Project", projectMessageHolder);
        }

        messageSender.sendAsyncMessages(issueTrackerModels);

        return new IssueTrackerResponse<>("Success", List.of());
    }

    private void logIssueTrackerMessages(String messageHolderType, IssueTrackerModelHolder<T> issueTrackerModels) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        List<String> creationModelAlertIds = issueTrackerModels.getIssueCreationModels().stream()
                .map(IssueCreationModel::getAlertIssueId)
                .map(UUID::toString)
                .toList();
        List<String> transitionModelAlertIds = issueTrackerModels.getIssueTransitionModels().stream()
            .map(IssueTransitionModel::getAlertIssueId)
            .map(UUID::toString)
            .toList();
        List<String> commentModelAlertIds = issueTrackerModels.getIssueCommentModels().stream()
            .map(IssueCommentModel::getAlertIssueId)
            .map(UUID::toString)
            .toList();
        logger.debug("{} Message Counts for Job execution id: {}, for {} notifications. Creation ({}): {}, Transition ({}): {}, Comment ({}): {}",
            messageHolderType,
            messageSender.getJobExecutionId(),
            messageSender.getNotificationIds().size(),
            issueTrackerModels.getIssueCreationModels().size(),
            creationModelAlertIds.isEmpty() ? "None" : creationModelAlertIds,
            issueTrackerModels.getIssueTransitionModels().size(),
            transitionModelAlertIds.isEmpty() ? "None" : transitionModelAlertIds,
            issueTrackerModels.getIssueCommentModels().size(),
            commentModelAlertIds.isEmpty() ? "None" : commentModelAlertIds);
    }

}
