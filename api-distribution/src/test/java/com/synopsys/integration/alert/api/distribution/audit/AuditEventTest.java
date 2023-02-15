package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    void constructorTest() {
        String destination = "destination";
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AuditEvent event = new AuditEvent(destination, jobExecutionId, notificationIds);

        assertEquals(destination, event.getDestination());
        assertEquals(jobExecutionId, event.getJobExecutionId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertNotNull(event.getCreatedTimestamp());
    }
}
