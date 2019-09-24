package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;

import com.synopsys.integration.alert.audit.mock.MockAuditEntryRestModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class AuditEntryRestModelTest extends RestModelTest<AuditEntryModel> {

    @Override
    public MockAuditEntryRestModel getMockUtil() {
        return new MockAuditEntryRestModel();
    }

    @Override
    public void assertRestModelFieldsFull(final AuditEntryModel restModel) {
        assertEquals(getMockUtil().getNotification(), restModel.getNotification());
        assertEquals(getMockUtil().getJobAuditModels(), restModel.getJobs());
        assertEquals(getMockUtil().getOverallStatus(), restModel.getOverallStatus());
        assertEquals(getMockUtil().getTimeLastSent(), restModel.getLastSent());
        assertEquals(getMockUtil().getId().toString(), restModel.getId());
    }

}
