package com.synopsys.integration.alert.api.channel.issue.send;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;

@ExtendWith(SpringExtension.class)
class IssueTrackerAsyncMessageSenderTest {
    @Mock
    private EventManager mockEventManager;
    @Mock
    private JobSubTaskAccessor mockJobSubTaskAccessor;

    @Test
    void sendAsyncMessagesNoEventsTest() {
        UUID parentEventId = UUID.randomUUID();
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
            mockJobSubTaskAccessor,
            parentEventId,
            jobExecutionId,
            Set.of(1L, 2L, 3L)
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(mockEventManager).sendEvent(Mockito.any());
        Mockito.verify(mockJobSubTaskAccessor, Mockito.times(0)).updateTaskCount(Mockito.eq(jobExecutionId), Mockito.anyLong());
    }

    @Test
    void sendAsyncMessageTest() {
        UUID parentEventId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();

        IssueCreationModel createModel = IssueCreationModel.simple("tile", "description", List.of(), new LinkableItem("Label", "Value"));
        IssueTransitionModel<String> transitionModel = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        IssueCommentModel<String> commentModel = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerModelHolder<String> modelHolder = new IssueTrackerModelHolder<>(List.of(createModel), List.of(transitionModel), List.of(commentModel));

        IssueTrackerCreationEventGenerator createEventGenerator = (model) -> new IssueTrackerCreateIssueEvent(null, parentEventId, jobExecutionId, jobId, null, createModel);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = (model) -> new IssueTrackerTransitionIssueEvent<>(
            null,
            parentEventId,
            jobExecutionId,
            jobId,
            null,
            null
        );
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = (model) -> new IssueTrackerCommentEvent<>(null, parentEventId, jobExecutionId, jobId, null, null);
        IssueTrackerAsyncMessageSender<String> sender = new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            mockEventManager,
            mockJobSubTaskAccessor,
            parentEventId,
            jobExecutionId,
            Set.of(1L, 2L, 3L)
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(mockEventManager).sendEvents(Mockito.any());
        Mockito.verify(mockJobSubTaskAccessor, Mockito.times(1)).updateTaskCount(Mockito.eq(jobExecutionId), Mockito.anyLong());
    }
}
