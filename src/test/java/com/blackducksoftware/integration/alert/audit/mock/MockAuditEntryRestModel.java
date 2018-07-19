package com.blackducksoftware.integration.alert.audit.mock;

import java.util.Date;

import com.blackducksoftware.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.audit.AuditEntryRestModel;
import com.blackducksoftware.integration.alert.web.model.NotificationRestModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MockAuditEntryRestModel extends MockRestModelUtil<AuditEntryRestModel> {
    private final String name;
    private final String eventType;
    private final String timeCreated;
    private final String timeLastSent;
    private final String status;
    private final NotificationRestModel notification;
    private final String errorMessage;
    private final String errorStackTrace;
    private final String id;

    public MockAuditEntryRestModel() {
        this("name", "eventType", new Date(400).toString(), new Date(500).toString(), AuditEntryStatus.SUCCESS.name(), new NotificationRestModel(), "errorMessage", "errorStackTrace", "1");
    }

    private MockAuditEntryRestModel(final String name, final String eventType, final String timeCreated, final String timeLastSent, final String status, final NotificationRestModel notification, final String errorMessage,
            final String errorStackTrace, final String id) {
        super();
        this.name = name;
        this.eventType = eventType;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.notification = notification;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public NotificationRestModel getNotification() {
        return notification;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public AuditEntryRestModel createRestModel() {
        return new AuditEntryRestModel(id, name, eventType, timeCreated, timeLastSent, status, errorMessage, errorStackTrace, notification);
    }

    @Override
    public AuditEntryRestModel createEmptyRestModel() {
        return new AuditEntryRestModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("eventType", eventType);
        json.addProperty("timeCreated", timeCreated);
        json.addProperty("timeLastSent", timeLastSent);
        json.addProperty("status", status);
        json.addProperty("errorStackTrace", errorStackTrace);
        json.addProperty("errorMessage", errorMessage);

        final Gson gson = new Gson();
        final JsonObject notificationJson = gson.toJsonTree(notification).getAsJsonObject();

        json.add("notification", notificationJson);
        return json.toString();
    }

}
