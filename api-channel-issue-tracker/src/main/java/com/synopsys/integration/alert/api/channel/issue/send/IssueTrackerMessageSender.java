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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.function.ThrowingFunction;

public class IssueTrackerMessageSender<T extends Serializable> {
    private final IssueTrackerIssueCreator<T> issueCreator;
    private final IssueTrackerIssueTransitioner<T> issueTransitioner;
    private final IssueTrackerIssueCommenter<T> issueCommenter;

    private final IssueTrackerCreationEventGenerator issueCreateEventGenerator;
    private final IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator;
    private final IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator;
    private final EventManager eventManager;

    public IssueTrackerMessageSender(
        IssueTrackerIssueCreator<T> issueCreator,
        IssueTrackerIssueTransitioner<T> issueTransitioner,
        IssueTrackerIssueCommenter<T> issueCommenter,
        IssueTrackerCreationEventGenerator issueCreateEventGenerator,
        IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator,
        IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator,
        EventManager eventManager
    ) {
        this.issueCreator = issueCreator;
        this.issueTransitioner = issueTransitioner;
        this.issueCommenter = issueCommenter;
        this.issueCreateEventGenerator = issueCreateEventGenerator;
        this.issueTrackerTransitionEventGenerator = issueTrackerTransitionEventGenerator;
        this.issueTrackerCommentEventGenerator = issueTrackerCommentEventGenerator;
        this.eventManager = eventManager;
    }

    // This method is used for testing issue tracker channels to get the issue that was created.
    public final List<IssueTrackerIssueResponseModel<T>> sendMessages(IssueTrackerModelHolder<T> issueTrackerMessage) throws AlertException {
        List<IssueTrackerIssueResponseModel<T>> responses = new LinkedList<>();

        List<IssueTrackerIssueResponseModel<T>> creationResponses = sendMessages(issueTrackerMessage.getIssueCreationModels(), issueCreator::createIssueTrackerIssue);
        responses.addAll(creationResponses);

        List<IssueTrackerIssueResponseModel<T>> transitionResponses = sendOptionalMessages(issueTrackerMessage.getIssueTransitionModels(), issueTransitioner::transitionIssue);
        responses.addAll(transitionResponses);

        List<IssueTrackerIssueResponseModel<T>> commentResponses = sendOptionalMessages(issueTrackerMessage.getIssueCommentModels(), issueCommenter::commentOnIssue);
        responses.addAll(commentResponses);

        return responses;
    }

    public final void sendAsyncMessages(IssueTrackerModelHolder<T> issueTrackerMessage) {
        sendAsyncMessages(issueTrackerMessage.getIssueCreationModels(), issueCreateEventGenerator::generateEvent);
        sendAsyncMessages(issueTrackerMessage.getIssueTransitionModels(), issueTrackerTransitionEventGenerator::generateEvent);
        sendAsyncMessages(issueTrackerMessage.getIssueCommentModels(), issueTrackerCommentEventGenerator::generateEvent);
    }

    public final List<IssueTrackerIssueResponseModel<T>> sendMessage(IssueCreationModel model) throws AlertException {
        return sendMessages(List.of(model), issueCreator::createIssueTrackerIssue);
    }

    public final List<IssueTrackerIssueResponseModel<T>> sendMessage(IssueTransitionModel<T> model) throws AlertException {
        return sendOptionalMessages(List.of(model), issueTransitioner::transitionIssue);
    }

    public final List<IssueTrackerIssueResponseModel<T>> sendMessage(IssueCommentModel<T> model) throws AlertException {
        return sendOptionalMessages(List.of(model), issueCommenter::commentOnIssue);
    }

    private <U> List<IssueTrackerIssueResponseModel<T>> sendMessages(List<U> messages, ThrowingFunction<U, IssueTrackerIssueResponseModel<T>, AlertException> sendMessage)
        throws AlertException {
        List<IssueTrackerIssueResponseModel<T>> responses = new LinkedList<>();
        for (U message : messages) {
            IssueTrackerIssueResponseModel<T> response = sendMessage.apply(message);
            responses.add(response);

        }
        return responses;
    }

    private <U> List<IssueTrackerIssueResponseModel<T>> sendOptionalMessages(
        List<U> messages,
        ThrowingFunction<U, Optional<IssueTrackerIssueResponseModel<T>>, AlertException> sendMessage
    ) throws AlertException {
        List<IssueTrackerIssueResponseModel<T>> responses = new LinkedList<>();
        for (U message : messages) {
            sendMessage
                .apply(message)
                .ifPresent(responses::add);
        }
        return responses;
    }

    private <U> void sendAsyncMessages(List<U> messages, Function<U, AlertEvent> eventGenerator) {
        for (U message : messages) {
            AlertEvent event = eventGenerator.apply(message);
            eventManager.sendEvent(event);
        }
    }
}
