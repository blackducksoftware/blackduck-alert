/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.component.audit.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.settings.RelationTest;

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
