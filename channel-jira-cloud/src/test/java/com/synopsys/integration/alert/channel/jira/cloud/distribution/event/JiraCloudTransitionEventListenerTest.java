package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.List;
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
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraCloudTransitionEventListenerTest {
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
        JiraCloudTransitionEvent event = new JiraCloudTransitionEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueTransitionModel
        );

        JiraCloudTransitionEventHandler handler = Mockito.spy(new JiraCloudTransitionEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraCloudTransitionEventListener listener = new JiraCloudTransitionEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_CLOUD, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);
        //TODO add assertions
    }
}
