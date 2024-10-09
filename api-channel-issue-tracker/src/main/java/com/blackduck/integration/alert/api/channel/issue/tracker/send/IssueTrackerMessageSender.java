package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.function.ThrowingFunction;

public class IssueTrackerMessageSender<T extends Serializable> {
    private final IssueTrackerIssueCreator<T> issueCreator;
    private final IssueTrackerIssueTransitioner<T> issueTransitioner;
    private final IssueTrackerIssueCommenter<T> issueCommenter;

    public IssueTrackerMessageSender(
        IssueTrackerIssueCreator<T> issueCreator,
        IssueTrackerIssueTransitioner<T> issueTransitioner,
        IssueTrackerIssueCommenter<T> issueCommenter
    ) {
        this.issueCreator = issueCreator;
        this.issueTransitioner = issueTransitioner;
        this.issueCommenter = issueCommenter;
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
}
