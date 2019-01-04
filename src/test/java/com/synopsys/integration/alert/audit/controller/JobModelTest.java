package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.audit.mock.MockJobModel;
import com.synopsys.integration.alert.web.audit.JobModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class JobModelTest extends RestModelTest<JobModel> {

    @Override
    public MockJobModel getMockUtil() {
        return new MockJobModel();
    }

    @Override
    public Class<JobModel> getRestModelClass() {
        return JobModel.class;
    }

    @Override
    public void assertRestModelFieldsNull(final JobModel restModel) {
        assertNull(restModel.getErrorMessage());
        assertNull(restModel.getErrorStackTrace());
        assertNull(restModel.getEventType());
        assertNull(restModel.getName());
        assertNull(restModel.getJobAuditModel());
        assertNull(restModel.getConfigId());
        assertNull(restModel.getId());
    }

    @Override
    public void assertRestModelFieldsFull(final JobModel restModel) {
        assertEquals(getMockUtil().getErrorMessage(), restModel.getErrorMessage());
        assertEquals(getMockUtil().getErrorStackTrace(), restModel.getErrorStackTrace());
        assertEquals(getMockUtil().getEventType(), restModel.getEventType());
        assertEquals(getMockUtil().getName(), restModel.getName());
        assertEquals(getMockUtil().getStatus(), restModel.getJobAuditModel().getStatus());
        assertEquals(getMockUtil().getTimeAuditCreated(), restModel.getJobAuditModel().getTimeAuditCreated());
        assertEquals(getMockUtil().getTimeLastSent(), restModel.getJobAuditModel().getTimeLastSent());
        assertEquals(getMockUtil().getConfigId(), restModel.getConfigId());
        assertEquals(getMockUtil().getId().toString(), restModel.getId());
    }

}
