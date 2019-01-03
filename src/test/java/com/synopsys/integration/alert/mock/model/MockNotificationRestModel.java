package com.synopsys.integration.alert.mock.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.web.model.NotificationConfig;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MockNotificationRestModel extends MockRestModelUtil<NotificationConfig> {
    private final String createdAt = new Date(400).toString();
    private final String provider = "provider";
    private final String providerCreationTime = new Date(300).toString();
    private final String notificationType = NotificationType.RULE_VIOLATION.name();
    private final String content = "{content: \" projectName projectVersion\"";
    private String id = "1";

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
