package com.synopsys.integration.alert.audit.mock;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.audit.JobModel;

public class MockJobModel extends MockRestModelUtil<JobModel> {
    private final String id;
    private final String configId;
    private final String name;
    private final String eventType;
    private final String timeAuditCreated;
    private final String timeLastSent;
    private final String status;
    private final String errorMessage;
    private final String errorStackTrace;

    public MockJobModel() {
        this("1", "22", "name", "eventType", new Date(400).toString(), new Date(500).toString(), AuditEntryStatus.SUCCESS.name(), "errorMessage", "errorStackTrace");
    }

    private MockJobModel(final String id, final String configId, final String name, final String eventType, final String timeAuditCreated, final String timeLastSent, final String status, final String errorMessage,
        final String errorStackTrace) {
        this.id = id;
        this.configId = configId;
        this.name = name;
        this.eventType = eventType;
        this.timeAuditCreated = timeAuditCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

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
    public JobModel createRestModel() {
        return new JobModel(id, configId, name, eventType, timeAuditCreated, timeLastSent, status, errorMessage, errorStackTrace);
    }

    @Override
    public JobModel createEmptyRestModel() {
        return new JobModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("configId", configId);
        json.addProperty("name", name);
        json.addProperty("eventType", eventType);
        json.addProperty("timeAuditCreated", timeAuditCreated);
        json.addProperty("timeLastSent", timeLastSent);
        json.addProperty("status", status);
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        return json.toString();
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }
}
