package com.synopsys.integration.alert.audit.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelationPK;

public class AuditNotificationRelationPKTest {

    @Test
    public void testGetAndSetMethods() {
        final AuditNotificationRelationPK primaryKey = new AuditNotificationRelationPK();
        final Long auditEntryId = 1L;
        final Long notificationId = 2L;
        primaryKey.setAuditEntryId(auditEntryId);
        primaryKey.setNotificationId(notificationId);

        assertEquals(auditEntryId, primaryKey.getAuditEntryId());
        assertEquals(notificationId, primaryKey.getNotificationId());
    }
}
