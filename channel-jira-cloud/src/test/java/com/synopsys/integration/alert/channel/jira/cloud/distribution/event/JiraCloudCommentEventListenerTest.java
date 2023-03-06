package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraCloudCommentEventListenerTest {
    private final Gson gson = new Gson();

    @Test
    void onMessageTest() {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

        IssueCommentModel<String> issueCommentModel = new IssueCommentModel<>(null, List.of("A comment"), null);
        JiraCloudCommentEvent event = new JiraCloudCommentEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCommentModel
        );

        JiraCloudCommentEventHandler handler = Mockito.spy(new JiraCloudCommentEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraCloudCommentEventListener listener = new JiraCloudCommentEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_CLOUD, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);
        //TODO add assertions
    }
}
