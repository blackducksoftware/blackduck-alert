package com.synopsys.integration.alert.web.api.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.synopsys.integration.alert.audit.mock.MockJobAuditModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class JobAuditModelTest extends RestModelTest<JobAuditModel> {

    @Override
    public MockJobAuditModel getMockUtil() {
        return new MockJobAuditModel();
    }

    @Override
    public void assertRestModelFieldsFull(JobAuditModel restModel) {
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
