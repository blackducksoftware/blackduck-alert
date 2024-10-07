package com.blackduck.integration.alert.component.audit.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals(getMockUtil().getCommonConfigId(), entity.getCommonConfigId());
        assertEquals(getMockUtil().getErrorMessage(), entity.getErrorMessage());
        assertEquals(getMockUtil().getErrorStackTrace(), entity.getErrorStackTrace());
        assertEquals(getMockUtil().getStatus().toString(), entity.getStatus());
        assertEquals(getMockUtil().getTimeCreated(), entity.getTimeCreated());
        assertEquals(getMockUtil().getTimeLastSent(), entity.getTimeLastSent());
    }

    @Override
    public MockAuditEntryEntity getMockUtil() {
        return new MockAuditEntryEntity();
    }

}
