package com.synopsys.integration.alert.api.distribution.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class JobStageStartedEventTest {

    @Test
    void eventContentTest() {
        UUID jobExecutionId = UUID.randomUUID();
        JobStage jobStage = JobStage.NOTIFICATION_PROCESSING;
        long startTimeMilli = Instant.now().toEpochMilli();
        JobStageStartedEvent event = new JobStageStartedEvent(jobExecutionId, jobStage, startTimeMilli);
        assertNotNull(event.getEventId());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(jobStage, event.getJobStage());
        assertEquals(startTimeMilli, event.getStartTimeMilliseconds());
    }
}
