package com.blackduck.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.audit.AuditFailedEvent;

class AuditFailedEventTest {
    @Test
    void constructorTest() {
        long testTime = Instant.now().toEpochMilli();
        UUID jobConfigId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "stack trace goees here";
        AuditFailedEvent event = new AuditFailedEvent(jobExecutionId, jobConfigId, notificationIds, errorMessage, stackTrace);

        assertEquals(AuditFailedEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(jobConfigId, event.getJobConfigId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertTrue(testTime <= event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isPresent());
        assertEquals(stackTrace, event.getStackTrace().get());
    }

    @Test
    void constructorStackTraceNullTest() {
        long testTime = Instant.now().toEpochMilli();
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        AuditFailedEvent event = new AuditFailedEvent(jobExecutionId, jobId, notificationIds, errorMessage, null);

        assertEquals(AuditFailedEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(jobId, event.getJobConfigId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertTrue(testTime <= event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isEmpty());
    }
}
