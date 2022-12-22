package com.synopsys.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

class ExecutingJobManagerTest {

    @Test
    void createExecutingJobTest() {
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        assertNotNull(executingJob);
        assertEquals(jobConfigId, executingJob.getJobConfigId());
    }

    @Test
    void executingJobPendingTest() {
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
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
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElseThrow(() -> new AssertionError("Job with execution ID not found."));
        savedJob.jobSucceeded();
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
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElseThrow(() -> new AssertionError("Job with execution ID not found."));
        savedJob.jobFailed();
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
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJobStage executingJobStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
        executingJob.addStage(executingJobStage);
        executingJobStage.endStage();
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
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJobStage executingJobStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
        executingJob.addStage(executingJobStage);
        executingJobStage.endStage();
        Optional<ExecutingJobStage> missingStage = executingJob.getStage(JobStage.CHANNEL_PROCESSING);
        assertTrue(missingStage.isEmpty());
    }

    @Test
    void addSameStageTest() {
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJobStage firstStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
        executingJob.addStage(firstStage);
        firstStage.endStage();

        ExecutingJobStage secondStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
        executingJob.addStage(secondStage);
        secondStage.endStage();

        ExecutingJobStage storedStage = executingJob.getStage(JobStage.NOTIFICATION_PROCESSING)
            .orElseThrow(() -> new AssertionError("Job Stage is missing when it should be present."));
        assertEquals(firstStage, storedStage);
        assertEquals(1, executingJob.getStages().size());
    }

    @Test
    void multipleStagesTest() {
        ExecutingJobManager jobManager = new ExecutingJobManager();
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId);
        ExecutingJobStage mappingStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.NOTIFICATION_PROCESSING);
        executingJob.addStage(mappingStage);
        mappingStage.endStage();
        ExecutingJobStage processingStage = ExecutingJobStage.createStage(executingJob.getExecutionId(), JobStage.CHANNEL_PROCESSING);
        executingJob.addStage(processingStage);
        processingStage.endStage();
        assertTrue(executingJob.getStage(JobStage.NOTIFICATION_PROCESSING).isPresent());
        assertTrue(executingJob.getStage(JobStage.CHANNEL_PROCESSING).isPresent());
        assertEquals(2, executingJob.getStages().size());
    }

}
