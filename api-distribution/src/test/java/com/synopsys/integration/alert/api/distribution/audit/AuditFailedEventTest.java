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
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "stack trace goees here";
        AuditFailedEvent event = new AuditFailedEvent(jobExecutionId, notificationIds, errorMessage, stackTrace);

        assertEquals(AuditFailedEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertNotNull(event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isPresent());
        assertEquals(stackTrace, event.getStackTrace().get());
    }

    @Test
    void constructorStackTraceNullTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        AuditFailedEvent event = new AuditFailedEvent(jobId, notificationIds, errorMessage, null);

        assertEquals(AuditFailedEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobId, event.getJobExecutionId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertNotNull(event.getCreatedTimestamp());
        assertEquals(errorMessage, event.getErrorMessage());
        assertTrue(event.getStackTrace().isEmpty());
    }
}
