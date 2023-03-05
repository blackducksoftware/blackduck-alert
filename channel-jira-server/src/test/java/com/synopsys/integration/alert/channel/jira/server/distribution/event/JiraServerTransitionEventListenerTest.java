package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockJobSubTaskStatusRepository;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;

class JiraServerTransitionEventListenerTest {
    private final Gson gson = new Gson();

    @Test
    void onMessageTest() {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

        IssueTransitionModel<String> issueTransitionModel = new IssueTransitionModel<>(null, IssueOperation.RESOLVE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            "destination",
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueTransitionModel
        );

        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();
        DefaultJobSubTaskAccessor jobSubTaskAccessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);
        JiraServerTransitionEventHandler handler = Mockito.spy(new JiraServerTransitionEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobId, 1L, notificationIds);
        Optional<JobSubTaskStatusModel> optionalJobSubTaskStatusModel = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertTrue(optionalJobSubTaskStatusModel.isPresent());

        JiraServerTransitionEventListener listener = new JiraServerTransitionEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        optionalJobSubTaskStatusModel = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertFalse(optionalJobSubTaskStatusModel.isPresent());
    }
}
