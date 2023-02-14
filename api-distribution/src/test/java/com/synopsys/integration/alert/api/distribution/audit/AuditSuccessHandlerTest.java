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
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.database.api.DefaultJobCompletionStatusModel;
import com.synopsys.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionRepository;

class AuditSuccessHandlerTest {
    private ExecutingJobManager executingJobManager;
    private com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModel jobCompletionStatusModel;

    @BeforeEach
    public void init() {
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);
        jobCompletionStatusModel = new DefaultJobCompletionStatusModel(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModel);
    }

    @Test
    void handleEventTest() {
        UUID jobId = UUID.randomUUID();
        ExecutingJob executingJob = executingJobManager.startJob(jobId, 0);
        UUID jobExecutionId = executingJob.getExecutionId();
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, Set.of());
        handler.handle(event);
        JobCompletionStatusModel statusModel = jobCompletionStatusModel.getJobExecutionStatus(jobId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS.name(), statusModel.getLatestStatus());
        assertEquals(1, statusModel.getSuccessCount());
        assertEquals(0, statusModel.getFailureCount());
        assertEquals(0, statusModel.getTotalNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void handleEventAuditMissingTest() {
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AlertPagedQueryDetails pagedQueryDetails = new AlertPagedQueryDetails(1, 10);
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, notificationIds);
        handler.handle(event);
        Optional<ExecutingJob> executingJob = executingJobManager.getExecutingJob(jobExecutionId);
        assertTrue(executingJob.isEmpty());
        assertTrue(jobCompletionStatusModel.getJobExecutionStatus(pagedQueryDetails).getModels().isEmpty());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }
}
