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
import com.synopsys.integration.alert.api.distribution.mock.MockJobExecutionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobExecutionStatusRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.database.api.DefaultJobExecutionStatusAccessor;
import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;

class AuditSuccessHandlerTest {
    private ExecutingJobManager executingJobManager;
    private JobExecutionStatusAccessor jobExecutionStatusAccessor;

    @BeforeEach
    public void init() {
        executingJobManager = new ExecutingJobManager();
        JobExecutionDurationsRepository jobExecutionDurationsRepository = new MockJobExecutionStatusDurationsRepository();
        JobExecutionRepository jobExecutionRepository = new MockJobExecutionStatusRepository(jobExecutionDurationsRepository);

        jobExecutionStatusAccessor = new DefaultJobExecutionStatusAccessor(jobExecutionRepository, jobExecutionDurationsRepository);
    }

    @Test
    void handleEventTest() {
        UUID jobId = UUID.randomUUID();
        ExecutingJob executingJob = executingJobManager.startJob(jobId, 0);
        UUID jobExecutionId = executingJob.getExecutionId();
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager, jobExecutionStatusAccessor);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, Set.of());
        handler.handle(event);
        JobExecutionStatusModel statusModel = jobExecutionStatusAccessor.getJobExecutionStatus(jobId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS.name(), statusModel.getLatestStatus());
        assertEquals(1, statusModel.getSuccessCount());
        assertEquals(0, statusModel.getFailureCount());
        assertEquals(0, statusModel.getNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void handleEventAuditMissingTest() {
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AlertPagedQueryDetails pagedQueryDetails = new AlertPagedQueryDetails(1, 10);
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager, jobExecutionStatusAccessor);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, notificationIds);
        handler.handle(event);
        Optional<ExecutingJob> executingJob = executingJobManager.getExecutingJob(jobExecutionId);
        assertTrue(executingJob.isEmpty());
        assertTrue(jobExecutionStatusAccessor.getJobExecutionStatus(pagedQueryDetails).getModels().isEmpty());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }
}
