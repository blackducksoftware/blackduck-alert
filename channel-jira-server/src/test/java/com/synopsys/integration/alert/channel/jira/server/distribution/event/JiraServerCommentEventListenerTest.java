package com.synopsys.integration.alert.channel.jira.server.distribution.event;

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
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

class JiraServerCommentEventListenerTest {
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

        IssueCommentModel<String> issueCommentModel = new IssueCommentModel<>(null, List.of("A comment"), null);
        JiraServerCommentEvent event = new JiraServerCommentEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCommentModel
        );

        JiraServerCommentEventHandler handler = Mockito.spy(new JiraServerCommentEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerCommentEventListener listener = new JiraServerCommentEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
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

        IssueCommentModel<String> issueCommentModel = new IssueCommentModel<>(null, List.of("A comment"), null);
        JiraServerCommentEvent event = new JiraServerCommentEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCommentModel
        );

        JiraServerCommentEventHandler handler = Mockito.spy(new JiraServerCommentEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerCommentEventListener listener = new JiraServerCommentEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
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
        executingJobManager.startStage(jobExecutionId, JobStage.ISSUE_COMMENTING, Instant.now());
        executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notificationIds.size());
        executingJobManager.incrementRemainingEvents(jobExecutionId, 2);

        IssueCommentModel<String> issueCommentModel = new IssueCommentModel<>(null, List.of("A comment"), null);
        JiraServerCommentEvent event = new JiraServerCommentEvent(
            "destination",
            jobExecutionId,
            jobId,
            notificationIds,
            issueCommentModel
        );

        JiraServerCommentEventHandler handler = Mockito.spy(new JiraServerCommentEventHandler(
            eventManager,
            gson,
            null,
            null,
            null,
            null,
            executingJobManager
        ));
        Mockito.doNothing().when(handler).handleEvent(event);

        JiraServerCommentEventListener listener = new JiraServerCommentEventListener(gson, new SyncTaskExecutor(), ChannelKeys.JIRA_SERVER, handler);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Expected to find and executing job but none found."));
        ExecutingJobStage jobStage = executingJob.getStage(JobStage.ISSUE_COMMENTING)
            .orElseThrow(() -> new AssertionError(String.format("Expected stage %s not found", JobStage.ISSUE_COMMENTING)));
        assertNotNull(jobStage.getStart());
        assertTrue(jobStage.getEnd().isEmpty());
    }
}
