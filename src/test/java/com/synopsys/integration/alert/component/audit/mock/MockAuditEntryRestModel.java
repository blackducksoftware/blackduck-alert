package com.synopsys.integration.alert.component.audit.mock;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;

public class MockAuditEntryRestModel extends MockRestModelUtil<AuditEntryModel> {
    private final String timeLastSent = new Date(500).toString();
    private final String overallStatus = AuditEntryStatus.SUCCESS.name();
    private final NotificationConfig notification = new NotificationConfig();
    private final List<JobAuditModel> jobAuditModels = Collections.singletonList(new JobAuditModel());
    private final String id = "1";

    public String getOverallStatus() {
        return overallStatus;
    }

    public NotificationConfig getNotification() {
        return notification;
    }

    public List<JobAuditModel> getJobAuditModels() {
        return jobAuditModels;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public AuditEntryModel createRestModel() {
        return new AuditEntryModel(id, notification, jobAuditModels, overallStatus, timeLastSent);
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("overallStatus", overallStatus);
        json.addProperty("lastSent", timeLastSent);

        final Gson gson = new Gson();
        final JsonObject notificationJson = gson.toJsonTree(notification).getAsJsonObject();
        json.add("notification", notificationJson);

        final Type listType = new TypeToken<List<JobAuditModel>>() {}.getType();
        final JsonArray jobModelJson = gson.toJsonTree(jobAuditModels, listType).getAsJsonArray();
        json.add("jobs", jobModelJson);

        return json.toString();
    }

}
