package com.synopsys.integration.alert.api.channel.issue.send;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

class IssueTrackerAsyncMessageSenderTest {
    private EventManager eventManager;
    private JobSubTaskAccessor jobSubTaskAccessor;

    @BeforeEach
    void init() {
        eventManager = Mockito.mock(EventManager.class);
        jobSubTaskAccessor = Mockito.mock(JobSubTaskAccessor.class);
    }

    @Test
    void sendAsyncMessagesNoEventsTest() {
        UUID jobId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        IssueTrackerModelHolder<String> modelHolder = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());

        IssueTrackerCreationEventGenerator createEventGenerator = (model) -> null;
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = (model) -> null;
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = (model) -> null;
        IssueTrackerAsyncMessageSender<String> sender = new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            eventManager,
            jobSubTaskAccessor,
            parentId,
            jobId,
            Set.of(1L, 2L, 3L)
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(eventManager).sendEvent(Mockito.any());
        Mockito.verify(jobSubTaskAccessor, Mockito.times(0)).updateTaskCount(Mockito.eq(parentId), Mockito.anyLong());
    }

    @Test
    void sendAsyncMessageTest() {
        UUID jobId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        IssueCreationModel createModel = IssueCreationModel.simple("tile", "description", List.of(), new LinkableItem("Label", "Value"));
        IssueTransitionModel<String> transitionModel = new IssueTransitionModel<>(null, IssueOperation.UPDATE, List.of(), null);
        IssueCommentModel<String> commentModel = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerModelHolder<String> modelHolder = new IssueTrackerModelHolder<>(List.of(createModel), List.of(transitionModel), List.of(commentModel));

        IssueTrackerCreationEventGenerator createEventGenerator = (model) -> new IssueTrackerCreateIssueEvent(null, parentId, jobId, null, createModel);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = (model) -> new IssueTrackerTransitionIssueEvent<>(null, parentId, jobId, null, null);
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = (model) -> new IssueTrackerCommentEvent<>(null, parentId, jobId, null, null);
        IssueTrackerAsyncMessageSender<String> sender = new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            eventManager,
            jobSubTaskAccessor,
            parentId,
            jobId,
            Set.of(1L, 2L, 3L)
        );

        sender.sendAsyncMessages(List.of(modelHolder));
        Mockito.verify(eventManager).sendEvents(Mockito.any());
        Mockito.verify(jobSubTaskAccessor, Mockito.times(1)).updateTaskCount(Mockito.eq(parentId), Mockito.anyLong());
    }
}
