package com.blackduck.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;

class JobStageEndedHandlerTest {

    @Test
    void handleTest() {
        ExecutingJobManager jobManager = createJobManager();
        JobStageEndedHandler handler = new JobStageEndedHandler(jobManager);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        UUID executionId = executingJob.getExecutionId();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        executingJob.addStage(new ExecutingJobStage(executionId, jobStage, Instant.now().minusSeconds(10)));
        long endTimeMilli = Instant.now().toEpochMilli();

        JobStageEndedEvent event = new JobStageEndedEvent(executionId, jobStage, endTimeMilli);

        handler.handle(event);
        ExecutingJobStage savedStage = jobManager.getExecutingJob(executionId)
            .flatMap(job -> job.getStage(jobStage))
            .orElseThrow(() -> new AssertionError("Job stage for execution not found."));
        assertEquals(jobStage, savedStage.getStage());
        assertNotNull(savedStage.getStart());
        assertTrue(savedStage.getStart().isBefore(Instant.ofEpochMilli(endTimeMilli)));
        assertEquals(Instant.ofEpochMilli(endTimeMilli), savedStage.getEnd().orElse(Instant.now()));
    }

    @Test
    void handleJobExecutionMissingTest() {
        ExecutingJobManager jobManager = createJobManager();
        JobStageEndedHandler handler = new JobStageEndedHandler(jobManager);
        UUID jobExecutionId = UUID.randomUUID();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        long endTimeMilli = Instant.now().toEpochMilli();
        JobStageEndedEvent event = new JobStageEndedEvent(jobExecutionId, jobStage, endTimeMilli);
        handler.handle(event);

        assertTrue(jobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void handleJobStageMissingTest() {
        ExecutingJobManager jobManager = createJobManager();
        JobStageEndedHandler handler = new JobStageEndedHandler(jobManager);
        UUID jobConfigId = UUID.randomUUID();
        ExecutingJob executingJob = jobManager.startJob(jobConfigId, 1);
        UUID executionId = executingJob.getExecutionId();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        Instant endTime = Instant.now();
        JobStageEndedEvent event = new JobStageEndedEvent(executionId, jobStage, endTime.toEpochMilli());
        handler.handle(event);

        Optional<ExecutingJobStage> stage = jobManager.getExecutingJob(executionId)
            .flatMap(job -> job.getStage(jobStage));
        assertTrue(stage.isEmpty());
    }

    private ExecutingJobManager createJobManager() {
        MockJobCompletionStatusDurationsRepository durationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(durationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, durationsRepository);
        return new ExecutingJobManager(jobCompletionStatusModelAccessor);
    }
}
