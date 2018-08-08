package com.blackducksoftware.integration.alert.mock.model;

import java.util.Date;

import com.blackducksoftware.integration.alert.database.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.web.model.NotificationConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MockNotificationRestModel extends MockRestModelUtil<NotificationConfig> {
    private final String createdAt;
    private final String provider;
    private final String notificationType;
    private final String content;
    private String id;

    @SuppressWarnings("deprecation")
    public MockNotificationRestModel() {
        this(new Date(400).toLocaleString(), "provider", NotificationCategoryEnum.POLICY_VIOLATION.name(), "{content: \" projectName projectVersion\"", "1");
    }

    private MockNotificationRestModel(final String createdAt, final String provider, final String notificationType, final String content, final String id) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.notificationType = notificationType;
        this.content = content;
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getContent() {
        return content;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public NotificationConfig createRestModel() {
        return new NotificationConfig(id, createdAt, provider, notificationType, content);
    }

    @Override
    public NotificationConfig createEmptyRestModel() {
        return new NotificationConfig();
    }

    @Override
    public String getRestModelJson() {
        final Gson gson = new Gson();
        final JsonObject json = new JsonObject();
        json.addProperty("createdAt", createdAt);
        json.addProperty("provider", provider);
        json.addProperty("notificationType", notificationType);
        json.addProperty("content", content);
        json.addProperty("id", id);
        return json.toString();
    }

}
