/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.function.ThrowingFunction;

public class IssueTrackerMessageSender<T extends Serializable> {
    private final IssueTrackerIssueCreator issueCreator;
    private final IssueTrackerIssueTransitioner<T> issueTransitioner;
    private final IssueTrackerIssueCommenter<T> issueCommenter;

    public IssueTrackerMessageSender(IssueTrackerIssueCreator issueCreator, IssueTrackerIssueTransitioner<T> issueTransitioner, IssueTrackerIssueCommenter<T> issueCommenter) {
        this.issueCreator = issueCreator;
        this.issueTransitioner = issueTransitioner;
        this.issueCommenter = issueCommenter;
    }

    public final IssueTrackerResponse sendMessages(List<IssueTrackerModelHolder<T>> channelMessages) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (IssueTrackerModelHolder<T> channelMessage : channelMessages) {
            List<IssueTrackerIssueResponseModel> creationResponses = sendMessages(channelMessage.getIssueCreationModels(), issueCreator::createIssueTrackerIssue);
            responses.addAll(creationResponses);

            List<IssueTrackerIssueResponseModel> transitionResponses = sendOptionalMessages(channelMessage.getIssueTransitionModels(), issueTransitioner::transitionIssue);
            responses.addAll(transitionResponses);

            List<IssueTrackerIssueResponseModel> commentResponses = sendOptionalMessages(channelMessage.getIssueCommentModels(), issueCommenter::commentOnIssue);
            responses.addAll(commentResponses);
        }
        return new IssueTrackerResponse("Success", responses);
    }

    private <U> List<IssueTrackerIssueResponseModel> sendMessages(List<U> messages, ThrowingFunction<U, IssueTrackerIssueResponseModel, AlertException> sendMessage) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (U message : messages) {
            IssueTrackerIssueResponseModel response = sendMessage.apply(message);
            responses.add(response);
        }
        return responses;
    }

    private <U> List<IssueTrackerIssueResponseModel> sendOptionalMessages(List<U> messages, ThrowingFunction<U, Optional<IssueTrackerIssueResponseModel>, AlertException> sendMessage) throws AlertException {
        List<IssueTrackerIssueResponseModel> responses = new LinkedList<>();
        for (U message : messages) {
            sendMessage
                .apply(message)
                .ifPresent(responses::add);
        }
        return responses;
    }

}
