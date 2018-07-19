package com.blackducksoftware.integration.alert.audit.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.entity.EntityTest;

public class AuditEntryEntityTest extends EntityTest<AuditEntryEntity> {

    @Override
    public Class<AuditEntryEntity> getEntityClass() {
        return AuditEntryEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(final AuditEntryEntity entity) {
        assertNull(entity.getCommonConfigId());
        assertNull(entity.getErrorMessage());
        assertNull(entity.getErrorStackTrace());
        assertNull(entity.getStatus());
        assertNull(entity.getTimeCreated());
        assertNull(entity.getTimeLastSent());
    }

    @Override
    public void assertEntityFieldsFull(final AuditEntryEntity entity) {
        assertEquals(getMockUtil().getCommonConfigId(), entity.getCommonConfigId());
        assertEquals(getMockUtil().getErrorMessage(), entity.getErrorMessage());
        assertEquals(getMockUtil().getErrorStackTrace(), entity.getErrorStackTrace());
        assertEquals(getMockUtil().getStatus(), entity.getStatus());
        assertEquals(getMockUtil().getTimeCreated(), entity.getTimeCreated());
        assertEquals(getMockUtil().getTimeLastSent(), entity.getTimeLastSent());
    }

    @Override
    public MockAuditEntryEntity getMockUtil() {
        return new MockAuditEntryEntity();
    }

}
