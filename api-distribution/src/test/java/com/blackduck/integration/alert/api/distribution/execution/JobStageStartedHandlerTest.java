package com.blackduck.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;

class JobStageStartedHandlerTest {

    @Test
    void handleTest() {
        ExecutingJobManager jobManager = createJobManager();
        JobStageStartedHandler handler = new JobStageStartedHandler(jobManager);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        UUID executionId = executingJob.getExecutionId();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        long startTimeMilli = Instant.now().toEpochMilli();
        JobStageStartedEvent event = new JobStageStartedEvent(executionId, jobStage, startTimeMilli);

        handler.handle(event);
        ExecutingJobStage savedStage = jobManager.getExecutingJob(executionId)
            .flatMap(job -> job.getStage(jobStage))
            .orElseThrow(() -> new AssertionError("Job stage for execution not found."));

        assertEquals(jobStage, savedStage.getStage());
        assertEquals(Instant.ofEpochMilli(startTimeMilli), savedStage.getStart());
    }

    @Test
    void handleJobExecutionMissingTest() {
        ExecutingJobManager jobManager = createJobManager();
        JobStageStartedHandler handler = new JobStageStartedHandler(jobManager);
        UUID jobExecutionId = UUID.randomUUID();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        long startTimeMilli = Instant.now().toEpochMilli();
        JobStageStartedEvent event = new JobStageStartedEvent(jobExecutionId, jobStage, startTimeMilli);
        handler.handle(event);

        assertTrue(jobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    private ExecutingJobManager createJobManager() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        return new ExecutingJobManager(jobCompletionStatusModelAccessor);
    }
}
