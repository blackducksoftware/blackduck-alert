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

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockJobSubTaskStatusRepository;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraServerCreateIssueEventListenerTest {
    private final Gson gson = new Gson();

    @Test
    void onMessageTest() {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();
        DefaultJobSubTaskAccessor jobSubTaskAccessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);
        JiraServerCreateIssueEventHandler handler = Mockito.spy(new JiraServerCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            null,
            null,
            null,
            null
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        jobSubTaskAccessor.createSubTaskStatus(jobExecutionId, jobId, 1L, notificationIds);
        Optional<JobSubTaskStatusModel> optionalJobSubTaskStatusModel = jobSubTaskAccessor.getSubTaskStatus(jobExecutionId);
        assertTrue(optionalJobSubTaskStatusModel.isPresent());

        JiraServerCreateIssueEventListener listener = new JiraServerCreateIssueEventListener(gson, ChannelKeys.JIRA_SERVER, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        optionalJobSubTaskStatusModel = jobSubTaskAccessor.getSubTaskStatus(jobExecutionId);
        assertFalse(optionalJobSubTaskStatusModel.isPresent());
    }
}
