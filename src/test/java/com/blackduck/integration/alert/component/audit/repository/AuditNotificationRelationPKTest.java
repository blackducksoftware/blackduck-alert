/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.repository;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.database.audit.AuditNotificationRelationPK;

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
