/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

@ExtendWith(SpringExtension.class)
class IssueTrackerAsyncMessageSenderTest {
    @Mock
    private EventManager mockEventManager;

    @Mock
    private ExecutingJobManager executingJobManager;

    @Test
    void sendAsyncMessagesNoEventsTest() {
        UUID jobExecutionId = UUID.randomUUID();

        IssueTrackerModelHolder<String> modelHolder = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());

        IssueTrackerCreationEventGenerator createEventGenerator = (model) -> null;
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = (model) -> null;
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = (model) -> null;
        DefaultIssueTrackerEventGenerator<String> eventGenerator = new DefaultIssueTrackerEventGenerator<>(createEventGenerator, transitionEventGenerator, commentEventGenerator);
        IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> sender = new IssueTrackerAsyncMessageSender<>(
            eventGenerator,
            mockEventManager,
            jobExecutionId,
            Set.of(1L, 2L, 3L),
            executingJobManager
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(mockEventManager, Mockito.times(0)).sendEvent(Mockito.any());

    }

    @Test
    void sendAsyncMessageTest() {
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();

        IssueCreationModel createModel = IssueCreationModel.simple("tile", "description", List.of(), new LinkableItem("Label", "Value"));
        IssueTransitionModel<String> transitionModel = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        IssueCommentModel<String> commentModel = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerModelHolder<String> modelHolder = new IssueTrackerModelHolder<>(List.of(createModel), List.of(transitionModel), List.of(commentModel));

        IssueTrackerCreationEventGenerator createEventGenerator = (model) -> new IssueTrackerCreateIssueEvent(null, jobExecutionId, jobId, null, createModel);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = (model) -> new IssueTrackerTransitionIssueEvent<>(
            null,
            jobExecutionId,
            jobId,
            null,
            null
        );
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = (model) -> new IssueTrackerCommentEvent<>(null, jobExecutionId, jobId, null, null);
        DefaultIssueTrackerEventGenerator<String> eventGenerator = new DefaultIssueTrackerEventGenerator<>(createEventGenerator, transitionEventGenerator, commentEventGenerator);
        IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> sender = new IssueTrackerAsyncMessageSender<>(
            eventGenerator,
            mockEventManager,
            jobExecutionId,
            Set.of(1L, 2L, 3L),
            executingJobManager
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(mockEventManager).sendEvents(Mockito.any());
    }
}
