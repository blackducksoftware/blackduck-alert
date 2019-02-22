package com.synopsys.integration.alert.audit.mock;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;

public class MockJobAuditModel extends MockRestModelUtil<JobAuditModel> {
    private final String id = "1";
    private final String configId = "22";
    private final String name = "name";
    private final String eventType = "eventType";
    private final String timeAuditCreated = new Date(400).toString();
    private final String timeLastSent = new Date(500).toString();
    private final String status = AuditEntryStatus.SUCCESS.name();
    private final String errorMessage = "errorMessage";
    private final String errorStackTrace = "errorStackTrace";

    public String getConfigId() {
        return configId;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTimeAuditCreated() {
        return timeAuditCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    @Override
    public JobAuditModel createRestModel() {
        return new JobAuditModel(id, configId, name, eventType, new AuditJobStatusModel(timeAuditCreated, timeLastSent, status), errorMessage, errorStackTrace);
    }

    @Override
    public JobAuditModel createEmptyRestModel() {
        return new JobAuditModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("configId", configId);
        json.addProperty("name", name);
        json.addProperty("eventType", eventType);

        final JsonObject auditInfo = new JsonObject();
        auditInfo.addProperty("timeAuditCreated", timeAuditCreated);
        auditInfo.addProperty("timeLastSent", timeLastSent);
        auditInfo.addProperty("status", status);

        json.add("auditJobStatusModel", auditInfo);
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        return json.toString();
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }
}
