package com.synopsys.integration.alert.mock.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.web.model.NotificationConfig;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MockNotificationRestModel extends MockRestModelUtil<NotificationConfig> {
    private final String createdAt;
    private final String provider;
    private final String providerCreationTime;
    private final String notificationType;
    private final String content;
    private String id;

    @SuppressWarnings("deprecation")
    public MockNotificationRestModel() {
        this(new Date(400).toLocaleString(), "provider", new Date(300).toLocaleString(), NotificationType.RULE_VIOLATION.name(), "{content: \" projectName projectVersion\"", "1");
    }

    private MockNotificationRestModel(final String createdAt, final String provider, final String providerCreationTime, final String notificationType, final String content, final String id) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerCreationTime = providerCreationTime;
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

    public String getProviderCreationTime() {
        return providerCreationTime;
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
        return new NotificationConfig(id, createdAt, provider, providerCreationTime, notificationType, content);
    }

    @Override
    public NotificationConfig createEmptyRestModel() {
        return new NotificationConfig();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("createdAt", createdAt);
        json.addProperty("provider", provider);
        json.addProperty("providerCreationTime", providerCreationTime);
        json.addProperty("notificationType", notificationType);
        json.addProperty("content", content);
        json.addProperty("id", id);
        return json.toString();
    }

}
