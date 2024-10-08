package com.synopsys.integration.alert.api.channel.issue.tracker.send;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

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
        IssueTrackerAsyncMessageSender<String> sender = new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
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
        IssueTrackerAsyncMessageSender<String> sender = new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            mockEventManager,
            jobExecutionId,
            Set.of(1L, 2L, 3L),
            executingJobManager
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(mockEventManager).sendEvents(Mockito.any());
    }
}
