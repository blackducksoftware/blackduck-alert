/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerEventModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.event.AlertEvent;

public class DefaultIssueTrackerEventGenerator<T extends Serializable> implements IssueTrackerEventGenerator<IssueTrackerModelHolder<T>> {

    private final IssueTrackerCreationEventGenerator issueCreateEventGenerator;
    private final IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator;
    private final IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator;

    public DefaultIssueTrackerEventGenerator(
        IssueTrackerCreationEventGenerator issueCreateEventGenerator,
        IssueTrackerTransitionEventGenerator<T> issueTrackerTransitionEventGenerator,
        IssueTrackerCommentEventGenerator<T> issueTrackerCommentEventGenerator
    ) {
        this.issueCreateEventGenerator = issueCreateEventGenerator;
        this.issueTrackerTransitionEventGenerator = issueTrackerTransitionEventGenerator;
        this.issueTrackerCommentEventGenerator = issueTrackerCommentEventGenerator;
    }

    @Override
    public IssueTrackerEventModel generateEvents(IssueTrackerModelHolder<T> model) {
        List<AlertEvent> creationEvents = createMessages(model.getIssueCreationModels(), issueCreateEventGenerator::generateEvent);
        List<AlertEvent> transitionEvents = createMessages(model.getIssueTransitionModels(), issueTrackerTransitionEventGenerator::generateEvent);
        List<AlertEvent> commentEvents = createMessages(model.getIssueCommentModels(), issueTrackerCommentEventGenerator::generateEvent);
        return new IssueTrackerEventModel(creationEvents, transitionEvents, commentEvents);
    }

    private <U> List<AlertEvent> createMessages(List<U> messages, Function<U, AlertEvent> eventGenerator) {
        return messages.stream()
            .map(eventGenerator)
            .toList();
    }
}
