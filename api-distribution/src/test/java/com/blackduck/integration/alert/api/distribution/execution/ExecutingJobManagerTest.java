/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;

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
        jobManager.updateJobStatus(executingJob.getExecutionId(), AuditEntryStatus.SUCCESS);
        jobManager.endJob(jobConfigId, Instant.now());
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
        jobManager.updateJobStatus(executingJob.getExecutionId(), AuditEntryStatus.SUCCESS);
        ExecutingJob savedJob = jobManager.getExecutingJob(executingJob.getExecutionId()).orElseThrow(() -> new AssertionError("Job expected to be saved."));
        jobManager.endJob(executingJob.getExecutionId(), Instant.now());
        AggregatedExecutionResults results = jobManager.aggregateExecutingJobData();
        assertEquals(jobConfigId, savedJob.getJobConfigId());
        assertEquals(AuditEntryStatus.SUCCESS, savedJob.getStatus());
        assertNotNull(executingJob.getStart());
        assertNotNull(executingJob.getEnd().orElseThrow(() -> new AssertionError("End time should be present for a completed job.")));

        assertEquals(0, results.getPendingJobs());
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
        jobManager.updateJobStatus(savedJob.getExecutionId(), AuditEntryStatus.FAILURE);
        jobManager.endJob(savedJob.getExecutionId(), Instant.now());
        AggregatedExecutionResults results = jobManager.aggregateExecutingJobData();
        assertEquals(jobConfigId, savedJob.getJobConfigId());
        assertEquals(AuditEntryStatus.FAILURE, executingJob.getStatus());
        assertNotNull(executingJob.getStart());
        assertNotNull(executingJob.getEnd().orElseThrow(() -> new AssertionError("End time should be present for a completed job.")));

        assertEquals(0, results.getPendingJobs());
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

    @Test
    void hasExpectedNotificationCountTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        UUID executionId = executingJob.getExecutionId();

        jobManager.incrementExpectedNotificationsSent(executionId, 1);
        assertFalse(jobManager.hasSentExpectedNotifications(executionId));

        jobManager.incrementSentNotificationCount(executionId, 1);
        assertTrue(jobManager.hasSentExpectedNotifications(executionId));
    }

    @Test
    void createMultipleExecutionsTest() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        ExecutingJobManager jobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob1 = jobManager.startJob(jobConfigId, 5);
        UUID executionId1 = executingJob1.getExecutionId();
        ExecutingJob executingJob2 = jobManager.startJob(jobConfigId, 10);
        UUID executionId2 = executingJob2.getExecutionId();
        jobManager.updateJobStatus(executionId1, AuditEntryStatus.SUCCESS);
        jobManager.updateJobStatus(executionId2, AuditEntryStatus.FAILURE);

        jobManager.incrementSentNotificationCount(executionId1, 5);
        jobManager.incrementSentNotificationCount(executionId2, 10);
        jobManager.incrementExpectedNotificationsSent(executionId1, 5);
        jobManager.incrementExpectedNotificationsSent(executionId2, 10);
        jobManager.endJob(executionId1, Instant.now());
        jobManager.endJob(executionId2, Instant.now());

        JobCompletionStatusModel jobCompletionStats = jobCompletionStatusModelAccessor.getJobExecutionStatus(jobConfigId).orElseThrow(() -> new AssertionError("Job completion status is missing when it should be present."));
        Assertions.assertEquals(1, jobCompletionStats.getSuccessCount());
        Assertions.assertEquals(1, jobCompletionStats.getFailureCount());
        Assertions.assertEquals(10,jobCompletionStats.getLatestNotificationCount());
        Assertions.assertEquals(15,jobCompletionStats.getTotalNotificationCount());
    }

}
