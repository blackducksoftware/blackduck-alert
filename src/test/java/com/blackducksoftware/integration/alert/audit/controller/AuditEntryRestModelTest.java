package com.blackducksoftware.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.audit.mock.MockAuditEntryRestModel;
import com.blackducksoftware.integration.alert.web.audit.AuditEntryConfig;
import com.blackducksoftware.integration.alert.web.model.RestModelTest;

public class AuditEntryRestModelTest extends RestModelTest<AuditEntryConfig> {

    @Override
    public MockAuditEntryRestModel getMockUtil() {
        return new MockAuditEntryRestModel();
    }

    @Override
    public Class<AuditEntryConfig> getRestModelClass() {
        return AuditEntryConfig.class;
    }

    @Override
    public void assertRestModelFieldsNull(final AuditEntryConfig restModel) {
        assertNull(restModel.getErrorMessage());
        assertNull(restModel.getErrorStackTrace());
        assertNull(restModel.getEventType());
        assertNull(restModel.getName());
        assertNull(restModel.getNotification());
        assertNull(restModel.getStatus());
        assertNull(restModel.getTimeCreated());
        assertNull(restModel.getTimeLastSent());
    }

    @Override
    public void assertRestModelFieldsFull(final AuditEntryConfig restModel) {
        assertEquals(getMockUtil().getErrorMessage(), restModel.getErrorMessage());
        assertEquals(getMockUtil().getErrorStackTrace(), restModel.getErrorStackTrace());
        assertEquals(getMockUtil().getEventType(), restModel.getEventType());
        assertEquals(getMockUtil().getName(), restModel.getName());
        assertEquals(getMockUtil().getNotification(), restModel.getNotification());
        assertEquals(getMockUtil().getStatus(), restModel.getStatus());
        assertEquals(getMockUtil().getTimeCreated(), restModel.getTimeCreated());
        assertEquals(getMockUtil().getTimeLastSent(), restModel.getTimeLastSent());
    }

}
