package com.synopsys.integration.alert.web.api.audit;

import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.alert.audit.mock.MockAuditEntryRestModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.web.model.RestModelTest;

public class AuditEntryRestModelTest extends RestModelTest<AuditEntryModel> {

    @Override
    public MockAuditEntryRestModel getMockUtil() {
        return new MockAuditEntryRestModel();
    }

    @Override
    public void assertRestModelFieldsFull(AuditEntryModel restModel) {
        Assertions.assertEquals(getMockUtil().getNotification(), restModel.getNotification());
        Assertions.assertEquals(getMockUtil().getJobAuditModels(), restModel.getJobs());
        Assertions.assertEquals(getMockUtil().getOverallStatus(), restModel.getOverallStatus());
        Assertions.assertEquals(getMockUtil().getTimeLastSent(), restModel.getLastSent());
        Assertions.assertEquals(getMockUtil().getId().toString(), restModel.getId());
    }

}
