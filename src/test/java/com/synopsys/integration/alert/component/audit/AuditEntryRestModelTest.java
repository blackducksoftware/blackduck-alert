package com.synopsys.integration.alert.component.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;

public class AuditEntryRestModelTest {

    @Test
    public void testRestModel() {
        String id = "1";
        String timeLastSent = new Date(500).toString();
        String overallStatus = AuditEntryStatus.SUCCESS.name();
        NotificationConfig notification = new NotificationConfig();
        List<JobAuditModel> jobAuditModels = Collections.singletonList(new JobAuditModel());

        AuditEntryModel restModel = new AuditEntryModel(id, notification, jobAuditModels, overallStatus, timeLastSent);

        assertEquals(notification, restModel.getNotification());
        assertEquals(jobAuditModels, restModel.getJobs());
        assertEquals(overallStatus, restModel.getOverallStatus());
        assertEquals(timeLastSent, restModel.getLastSent());
        assertEquals(id, restModel.getId());
    }

}
