package com.blackduck.integration.alert.channel.jira.server.distribution.event;

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

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class JiraServerTransitionEventListenerTest {
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
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueTransitionModel
        );

        JiraServerTransitionEventHandler handler = Mockito.spy(new JiraServerTransitionEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerTransitionEventListener listener = new JiraServerTransitionEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
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
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueTransitionModel
        );

        JiraServerTransitionEventHandler handler = Mockito.spy(new JiraServerTransitionEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerTransitionEventListener listener = new JiraServerTransitionEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
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
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueTransitionModel
        );

        JiraServerTransitionEventHandler handler = Mockito.spy(new JiraServerTransitionEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerTransitionEventListener listener = new JiraServerTransitionEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Expected to find and executing job but none found."));
        ExecutingJobStage jobStage = executingJob.getStage(JobStage.ISSUE_TRANSITION)
            .orElseThrow(() -> new AssertionError(String.format("Expected stage %s not found", JobStage.ISSUE_TRANSITION)));
        assertNotNull(jobStage.getStart());
        assertTrue(jobStage.getEnd().isEmpty());
    }
}
