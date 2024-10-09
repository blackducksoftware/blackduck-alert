package com.blackduck.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.audit.AuditEvent;

class AuditEventTest {

    @Test
    void constructorTest() {
        long testTime = Instant.now().toEpochMilli();
        String destination = "destination";
        UUID jobConfigId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AuditEvent event = new AuditEvent(destination, jobExecutionId, jobConfigId, notificationIds);

        assertEquals(destination, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertTrue(testTime <= event.getCreatedTimestamp());
    }
}
