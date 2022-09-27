package com.synopsys.integration.alert.database.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class NotificationCorrelationToNotificationRelationTest {
    @Test
    void defaultConstructorTest() {
        NotificationCorrelationToNotificationRelation relation = new NotificationCorrelationToNotificationRelation();
        assertNull(relation.getNotificationCorrelationId());
        assertNull(relation.getNotificationId());
    }

    @Test
    void constructorTest() {
        UUID correlationId = UUID.randomUUID();
        Long notificationId = 1L;
        NotificationCorrelationToNotificationRelation relation = new NotificationCorrelationToNotificationRelation(correlationId, notificationId);
        assertEquals(correlationId, relation.getNotificationCorrelationId());
        assertEquals(notificationId, relation.getNotificationId());
    }
}
