/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;
import com.blackduck.integration.alert.database.settings.RelationTest;

public class AuditNotificationRelationTest extends RelationTest<AuditNotificationRelation> {

    @Override
    public Class<AuditNotificationRelation> getEntityClass() {
        return AuditNotificationRelation.class;
    }

    @Override
    public void assertEntityFieldsNull(final AuditNotificationRelation entity) {
        assertNull(entity.getAuditEntryId());
        assertNull(entity.getNotificationId());
    }

    @Override
    public void assertEntityFieldsFull(final AuditNotificationRelation entity) {
        assertNotNull(entity.getAuditEntryId());
        assertNotNull(entity.getNotificationId());
    }

    @Override
    public AuditNotificationRelation createMockRelation(final Long firstId, final Long secondId) {
        return new AuditNotificationRelation(firstId, secondId);
    }

    @Override
    public AuditNotificationRelation createMockEmptyRelation() {
        return new AuditNotificationRelation();
    }

}
