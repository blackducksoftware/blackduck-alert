package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

class JiraCloudTransitionEventListenerTest {
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
        executingJobManager.startStage(jobExecutionId, JobStage.ISSUE_TRANSITION, Instant.now());
        executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notificationIds.size());
        executingJobManager.incrementRemainingEvents(jobExecutionId, 2);

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

        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Expected to find and executing job but none found."));
        ExecutingJobStage jobStage = executingJob.getStage(JobStage.ISSUE_TRANSITION)
            .orElseThrow(() -> new AssertionError(String.format("Expected stage %s not found", JobStage.ISSUE_TRANSITION)));
        assertNotNull(jobStage.getStart());
        assertTrue(jobStage.getEnd().isEmpty());
    }
}
