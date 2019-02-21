package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.audit.mock.MockJobAuditModel;
import com.synopsys.integration.alert.common.data.model.JobAuditModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class JobAuditModelTest extends RestModelTest<JobAuditModel> {

    @Override
    public MockJobAuditModel getMockUtil() {
        return new MockJobAuditModel();
    }

    @Override
    public Class<JobAuditModel> getRestModelClass() {
        return JobAuditModel.class;
    }

    @Override
    public void assertRestModelFieldsNull(final JobAuditModel restModel) {
        assertNull(restModel.getErrorMessage());
        assertNull(restModel.getErrorStackTrace());
        assertNull(restModel.getEventType());
        assertNull(restModel.getName());
        assertNull(restModel.getAuditJobStatusModel());
        assertNull(restModel.getConfigId());
        assertNull(restModel.getId());
    }

    @Override
    public void assertRestModelFieldsFull(final JobAuditModel restModel) {
        assertEquals(getMockUtil().getErrorMessage(), restModel.getErrorMessage());
        assertEquals(getMockUtil().getErrorStackTrace(), restModel.getErrorStackTrace());
        assertEquals(getMockUtil().getEventType(), restModel.getEventType());
        assertEquals(getMockUtil().getName(), restModel.getName());
        assertEquals(getMockUtil().getStatus(), restModel.getAuditJobStatusModel().getStatus());
        assertEquals(getMockUtil().getTimeAuditCreated(), restModel.getAuditJobStatusModel().getTimeAuditCreated());
        assertEquals(getMockUtil().getTimeLastSent(), restModel.getAuditJobStatusModel().getTimeLastSent());
        assertEquals(getMockUtil().getConfigId(), restModel.getConfigId());
        assertEquals(getMockUtil().getId().toString(), restModel.getId());
    }

}
