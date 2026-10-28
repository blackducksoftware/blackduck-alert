/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit.repository;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Assertions;

import com.blackduck.integration.alert.component.audit.mock.MockAuditEntryEntity;
import com.blackduck.integration.alert.database.audit.AuditEntryEntity;
import com.blackduck.integration.alert.database.settings.EntityTest;

public class AuditEntryEntityTest extends EntityTest<AuditEntryEntity> {

    @Override
    public Class<AuditEntryEntity> getEntityClass() {
        return AuditEntryEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(AuditEntryEntity entity) {
        assertNull(entity.getCommonConfigId());
        assertNull(entity.getErrorMessage());
        assertNull(entity.getErrorStackTrace());
        assertNull(entity.getStatus());
        assertNull(entity.getTimeCreated());
        assertNull(entity.getTimeLastSent());
    }

    @Override
    public void assertEntityFieldsFull(AuditEntryEntity entity) {
        Assertions.assertEquals(getMockUtil().getCommonConfigId(), entity.getCommonConfigId());
        Assertions.assertEquals(getMockUtil().getErrorMessage(), entity.getErrorMessage());
        Assertions.assertEquals(getMockUtil().getErrorStackTrace(), entity.getErrorStackTrace());
        Assertions.assertEquals(getMockUtil().getStatus().toString(), entity.getStatus());
        Assertions.assertEquals(getMockUtil().getTimeCreated(), entity.getTimeCreated());
        Assertions.assertEquals(getMockUtil().getTimeLastSent(), entity.getTimeLastSent());
    }

    @Override
    public MockAuditEntryEntity getMockUtil() {
        return new MockAuditEntryEntity();
    }

}
