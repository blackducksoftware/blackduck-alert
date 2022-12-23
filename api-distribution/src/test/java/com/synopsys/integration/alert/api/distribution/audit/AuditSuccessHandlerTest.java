package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

class AuditSuccessHandlerTest {
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        executingJobManager = new ExecutingJobManager();
    }

    @Test
    void handleEventTest() {
        UUID jobId = UUID.randomUUID();
        ExecutingJob executingJob = executingJobManager.startJob(jobId, 0);
        UUID jobExecutionId = executingJob.getExecutionId();
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, Set.of());
        handler.handle(event);
        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS, executingJob.getStatus());
    }

    @Test
    void handleEventAuditMissingTest() {
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, notificationIds);
        handler.handle(event);
        Optional<ExecutingJob> executingJob = executingJobManager.getExecutingJob(jobExecutionId);
        assertTrue(executingJob.isEmpty());
    }
}
