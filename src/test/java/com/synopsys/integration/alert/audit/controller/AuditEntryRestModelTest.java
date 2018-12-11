package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.audit.mock.MockAuditEntryRestModel;
import com.synopsys.integration.alert.web.audit.AuditEntryModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class AuditEntryRestModelTest extends RestModelTest<AuditEntryModel> {

    @Override
    public MockAuditEntryRestModel getMockUtil() {
        return new MockAuditEntryRestModel();
    }

    @Override
    public Class<AuditEntryModel> getRestModelClass() {
        return AuditEntryModel.class;
    }

    @Override
    public void assertRestModelFieldsNull(final AuditEntryModel restModel) {
        assertNull(restModel.getNotification());
        assertNull(restModel.getJobs());
        assertNull(restModel.getOverallStatus());
        assertNull(restModel.getLastSent());
        assertNull(restModel.getId());
    }

    @Override
    public void assertRestModelFieldsFull(final AuditEntryModel restModel) {
        assertEquals(getMockUtil().getNotification(), restModel.getNotification());
        assertEquals(getMockUtil().getJobModels(), restModel.getJobs());
        assertEquals(getMockUtil().getOverallStatus(), restModel.getOverallStatus());
        assertEquals(getMockUtil().getTimeLastSent(), restModel.getLastSent());
        assertEquals(getMockUtil().getId().toString(), restModel.getId());
    }

}
