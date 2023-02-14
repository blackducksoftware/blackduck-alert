package com.synopsys.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.database.api.DefaultJobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.database.job.execution.JobCompletionRepository;

class ExecutingJobManagerTest {

    @Test
    void createExecutingJobTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 0);
        assertNotNull(executingJob);
        assertEquals(jobConfigId, executingJob.getJobConfigId());
    }

    @Test
    void removeExecutingJobTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 0);
        jobManager.endJobWithSuccess(jobConfigId, Instant.now());
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElse(null);
        jobManager.purgeJob(executingJob.getExecutionId());
        assertNotNull(savedJob);
        assertEquals(executingJob.getExecutionId(), savedJob.getExecutionId());
        assertTrue(jobManager.getExecutingJob(savedJob.getExecutionId()).isEmpty());
    }

    @Test
    void executingJobPendingTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        AggregatedExecutionResults results = jobManager.aggregateExecutingJobData();
        assertNotNull(executingJob);
        assertEquals(jobConfigId, executingJob.getJobConfigId());
        assertEquals(AuditEntryStatus.PENDING, executingJob.getStatus());
        assertNotNull(executingJob.getStart());
        assertTrue(executingJob.getEnd().isEmpty());

        assertEquals(1, results.getPendingJobs());
        assertEquals(0, results.getSuccessFulJobs());
        assertEquals(0, results.getFailedJobs());
        assertEquals(1, results.getTotalJobsInSystem());
    }

    @Test
    void executingJobSucceededTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElseThrow(() -> new AssertionError("Job with execution ID not found."));
        savedJob.jobSucceeded(Instant.now());
        AggregatedExecutionResults results = jobManager.aggregateExecutingJobData();
        assertEquals(jobConfigId, savedJob.getJobConfigId());
        assertEquals(AuditEntryStatus.SUCCESS, savedJob.getStatus());
        assertNotNull(executingJob.getStart());
        assertNotNull(executingJob.getEnd().orElseThrow(() -> new AssertionError("End time should be present for a completed job.")));

        assertEquals(0, results.getPendingJobs());
        assertEquals(1, results.getSuccessFulJobs());
        assertEquals(0, results.getFailedJobs());
        assertEquals(1, results.getTotalJobsInSystem());
    }

    @Test
    void executingJobFailedTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElseThrow(() -> new AssertionError("Job with execution ID not found."));
        savedJob.jobFailed(Instant.now());
        AggregatedExecutionResults results = jobManager.aggregateExecutingJobData();
        assertEquals(jobConfigId, savedJob.getJobConfigId());
        assertEquals(AuditEntryStatus.FAILURE, executingJob.getStatus());
        assertNotNull(executingJob.getStart());
        assertNotNull(executingJob.getEnd().orElseThrow(() -> new AssertionError("End time should be present for a completed job.")));

        assertEquals(0, results.getPendingJobs());
        assertEquals(0, results.getSuccessFulJobs());
        assertEquals(1, results.getFailedJobs());
        assertEquals(1, results.getTotalJobsInSystem());
    }

    @Test
    void addStageTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJobStage executingJobStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
        executingJob.addStage(executingJobStage);
        executingJobStage.endStage(Instant.now());
        ExecutingJobStage storedStage = executingJob.getStage(JobStage.NOTIFICATION_PROCESSING)
            .orElseThrow(() -> new AssertionError("Job Stage is missing when it should be present."));
        assertEquals(executingJobStage, storedStage);
        assertEquals(executingJob.getExecutionId(), executingJobStage.getExecutionId());
        assertEquals(JobStage.NOTIFICATION_PROCESSING, executingJobStage.getStage());
        assertNotNull(executingJobStage.getStart());
        assertNotNull(executingJobStage.getEnd());
    }

    @Test
    void stageMissingTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJobStage executingJobStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
        executingJob.addStage(executingJobStage);
        executingJobStage.endStage(Instant.now());
        Optional<ExecutingJobStage> missingStage = executingJob.getStage(JobStage.CHANNEL_PROCESSING);
        assertTrue(missingStage.isEmpty());
    }

    @Test
    void addSameStageTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJobStage firstStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
        executingJob.addStage(firstStage);
        firstStage.endStage(Instant.now());

        ExecutingJobStage secondStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
        executingJob.addStage(secondStage);
        secondStage.endStage(Instant.now());

        ExecutingJobStage storedStage = executingJob.getStage(JobStage.NOTIFICATION_PROCESSING)
            .orElseThrow(() -> new AssertionError("Job Stage is missing when it should be present."));
        assertEquals(firstStage, storedStage);
        assertEquals(1, executingJob.getStages().size());
    }

    @Test
    void multipleStagesTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        ExecutingJobStage mappingStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING, Instant.now());
        executingJob.addStage(mappingStage);
        mappingStage.endStage(Instant.now());
        ExecutingJobStage processingStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.CHANNEL_PROCESSING, Instant.now());
        executingJob.addStage(processingStage);
        processingStage.endStage(Instant.now());
        assertTrue(executingJob.getStage(JobStage.NOTIFICATION_PROCESSING).isPresent());
        assertTrue(executingJob.getStage(JobStage.CHANNEL_PROCESSING).isPresent());
        assertEquals(2, executingJob.getStages().size());
    }

}
