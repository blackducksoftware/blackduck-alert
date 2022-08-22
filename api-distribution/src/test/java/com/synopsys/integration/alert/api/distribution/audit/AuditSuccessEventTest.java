package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditSuccessEventTest {

    @Test
    void constructorTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AuditEvent event = new AuditSuccessEvent(jobId, notificationIds);

        assertEquals(AuditSuccessEvent.DEFAULT_DESTINATION_NAME, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(notificationIds, event.getNotificationIds());
        assertNotNull(event.getCreatedTimestamp());
    }
}
