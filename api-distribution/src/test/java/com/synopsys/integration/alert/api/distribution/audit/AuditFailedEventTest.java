package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditFailedEventTest {
    @Test
    void constructorTest() {
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
        assertNotNull(event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isPresent());
        assertEquals(stackTrace, event.getStackTrace().get());
    }

    @Test
    void constructorStackTraceNullTest() {
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        AuditFailedEvent event = new AuditFailedEvent(jobExecutionId, jobId, notificationIds, errorMessage, null);

        assertEquals(AuditFailedEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(jobId, event.getJobConfigId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertNotNull(event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isEmpty());
    }
}
