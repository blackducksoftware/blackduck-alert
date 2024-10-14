/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.rest.model.JobAuditModel;

public class JobAuditModelTest {
    @Test
    public void testModel() {
        String id = "1";
        String configId = "22";
        String name = "name";
        String eventType = "eventType";
        String timeAuditCreated = new Date(400).toString();
        String timeLastSent = new Date(500).toString();
        String status = AuditEntryStatus.SUCCESS.name();
        String errorMessage = "errorMessage";
        String errorStackTrace = "errorStackTrace";
        AuditJobStatusModel auditJobStatusModel = new AuditJobStatusModel(UUID.randomUUID(), timeAuditCreated, timeLastSent, status);

        JobAuditModel restModel = new JobAuditModel(id, configId, name, eventType, auditJobStatusModel, errorMessage, errorStackTrace);

        assertEquals(errorMessage, restModel.getErrorMessage());
        assertEquals(errorStackTrace, restModel.getErrorStackTrace());
        assertEquals(eventType, restModel.getEventType());
        assertEquals(name, restModel.getName());
        assertEquals(status, restModel.getAuditJobStatusModel().getStatus());
        assertEquals(timeAuditCreated, restModel.getAuditJobStatusModel().getTimeAuditCreated());
        assertEquals(timeLastSent, restModel.getAuditJobStatusModel().getTimeLastSent());
        assertEquals(configId, restModel.getConfigId());
        assertEquals(id, restModel.getId());
    }

}
