package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class JiraCloudCreateIssueEventListenerTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    void onMessageTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = Mockito.mock(JobCompletionStatusModelAccessor.class);
        ExecutingJobManager executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);

        ExecutingJob executingJob = executingJobManager.startJob(jobId, notificationIds.size());
        UUID jobExecutionId = executingJob.getExecutionId();
        executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notificationIds.size());

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

        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void onMessageJobWithRemainingEventsFinishedTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = Mockito.mock(JobCompletionStatusModelAccessor.class);
        ExecutingJobManager executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);

        ExecutingJob executingJob = executingJobManager.startJob(jobId, notificationIds.size());
        UUID jobExecutionId = executingJob.getExecutionId();
        executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notificationIds.size());
        executingJobManager.incrementRemainingEvents(jobExecutionId, 1);

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

        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void onMessageJobWithRemainingEventsTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        EventManager eventManager = Mockito.mock(EventManager.class);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = Mockito.mock(JobCompletionStatusModelAccessor.class);
        ExecutingJobManager executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);

        ExecutingJob executingJob = executingJobManager.startJob(jobId, notificationIds.size());
        UUID jobExecutionId = executingJob.getExecutionId();
        executingJobManager.startStage(jobExecutionId, JobStage.ISSUE_CREATION, Instant.now());
        executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notificationIds.size());
        executingJobManager.incrementRemainingEvents(jobExecutionId, 2);

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

        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Expected to find and executing job but none found."));
        ExecutingJobStage jobStage = executingJob.getStage(JobStage.ISSUE_CREATION)
            .orElseThrow(() -> new AssertionError(String.format("Expected stage %s not found", JobStage.ISSUE_CREATION)));
        assertNotNull(jobStage.getStart());
        assertTrue(jobStage.getEnd().isEmpty());
    }
}
