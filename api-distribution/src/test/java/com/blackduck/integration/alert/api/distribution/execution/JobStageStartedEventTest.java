/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

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
