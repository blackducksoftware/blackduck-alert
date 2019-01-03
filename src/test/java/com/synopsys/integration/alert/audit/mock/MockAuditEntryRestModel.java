package com.synopsys.integration.alert.audit.mock;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.audit.AuditEntryModel;
import com.synopsys.integration.alert.web.audit.JobModel;
import com.synopsys.integration.alert.web.model.NotificationConfig;

public class MockAuditEntryRestModel extends MockRestModelUtil<AuditEntryModel> {
    private final String timeLastSent = new Date(500).toString();
    private final String overallStatus = AuditEntryStatus.SUCCESS.name();
    private final NotificationConfig notification = new NotificationConfig();
    private final List<JobModel> jobModels = Collections.singletonList(new JobModel());
    private final String id = "1";

    public String getOverallStatus() {
        return overallStatus;
    }

    public NotificationConfig getNotification() {
        return notification;
    }

    public List<JobModel> getJobModels() {
        return jobModels;
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
        return new AuditEntryModel(id, notification, jobModels, overallStatus, timeLastSent);
    }

    @Override
    public AuditEntryModel createEmptyRestModel() {
        return new AuditEntryModel();
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

        final Type listType = new TypeToken<List<JobModel>>() {}.getType();
        final JsonArray jobModelJson = gson.toJsonTree(jobModels, listType).getAsJsonArray();
        json.add("jobs", jobModelJson);

        return json.toString();
    }

}
