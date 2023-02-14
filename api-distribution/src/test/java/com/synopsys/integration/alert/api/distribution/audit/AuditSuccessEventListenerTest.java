package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.synopsys.integration.alert.database.api.DefaultJobCompletionStatusModel;
import com.synopsys.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionRepository;

class AuditSuccessEventListenerTest {
    private final Gson gson = new Gson();
    private final TaskExecutor taskExecutor = new SyncTaskExecutor();

    private ExecutingJobManager executingJobManager;
    private com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModel jobCompletionStatusModel;
    private AuditSuccessHandler handler;

    @BeforeEach
    public void init() {
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);
        jobCompletionStatusModel = new DefaultJobCompletionStatusModel(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModel);
        handler = new AuditSuccessHandler(executingJobManager);
    }

    @Test
    void onMessageTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        ExecutingJob executingJob = executingJobManager.startJob(jobId, notificationIds.size());
        UUID executingJobId = executingJob.getExecutionId();

        AuditSuccessEventListener listener = new AuditSuccessEventListener(gson, taskExecutor, handler);
        AuditSuccessEvent event = new AuditSuccessEvent(executingJobId, notificationIds);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        JobCompletionStatusModel statusModel = jobCompletionStatusModel.getJobExecutionStatus(jobId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS.name(), statusModel.getLatestStatus());
        assertEquals(1, statusModel.getSuccessCount());
        assertEquals(0, statusModel.getFailureCount());
        assertEquals(0, statusModel.getTotalNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(executingJobId).isEmpty());
    }
}
