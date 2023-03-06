package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraCloudCreateIssueEventListenerTest {
    private final Gson gson = new Gson();

    @Test
    void onMessageTest() {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraCloudCreateIssueEvent event = new JiraCloudCreateIssueEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        JiraCloudCreateIssueEventHandler handler = Mockito.spy(new JiraCloudCreateIssueEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraCloudCreateIssueEventListener listener = new JiraCloudCreateIssueEventListener(gson, ChannelKeys.JIRA_CLOUD, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

    }
}
