package com.synopsys.integration.alert.database.api.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationPK;

class NotificationCorrelationToNotificationRelationPKTest {
    @Test
    void defaultConstructorTest() {
        NotificationCorrelationToNotificationRelationPK relation = new NotificationCorrelationToNotificationRelationPK();
        assertNull(relation.getNotificationCorrelationId());
        assertNull(relation.getNotificationId());
    }

    @Test
    void constructorTest() {
        UUID correlationId = UUID.randomUUID();
        Long notificationId = 1L;
        NotificationCorrelationToNotificationRelationPK relation = new NotificationCorrelationToNotificationRelationPK(correlationId, notificationId);
        assertEquals(correlationId, relation.getNotificationCorrelationId());
        assertEquals(notificationId, relation.getNotificationId());
    }

    @Test
    void setMethodTest() {
        UUID correlationId = UUID.randomUUID();
        Long notificationId = 1L;
        NotificationCorrelationToNotificationRelationPK relation = new NotificationCorrelationToNotificationRelationPK(correlationId, notificationId);
        assertEquals(correlationId, relation.getNotificationCorrelationId());
        assertEquals(notificationId, relation.getNotificationId());
        correlationId = UUID.randomUUID();
        notificationId = 2L;
        relation.setNotificationCorrelationId(correlationId);
        relation.setNotificationId(notificationId);
        assertEquals(correlationId, relation.getNotificationCorrelationId());
        assertEquals(notificationId, relation.getNotificationId());
    }
}
