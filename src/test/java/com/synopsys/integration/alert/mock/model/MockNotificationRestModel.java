package com.synopsys.integration.alert.mock.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class MockNotificationRestModel extends MockRestModelUtil<NotificationConfig> {
    private final String createdAt = new Date(400).toString();
    private final String provider = "provider";
    private final Long providerConfigId = 1L;
    private final String providerConfigName = "provider config";
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

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProviderConfigName() {
        return providerConfigName;
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

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public NotificationConfig createRestModel() {
        return new NotificationConfig(id, createdAt, provider, providerConfigId, providerConfigName, providerCreationTime, notificationType, content);
    }

    @Override
    public String getRestModelJson() {
        JsonObject json = new JsonObject();
        json.addProperty("createdAt", createdAt);
        json.addProperty("provider", provider);
        json.addProperty("providerConfigId", providerConfigId);
        json.addProperty("providerConfigName", providerConfigName);
        json.addProperty("providerCreationTime", providerCreationTime);
        json.addProperty("notificationType", notificationType);
        json.addProperty("content", content);
        json.addProperty("id", id);
        return json.toString();
    }

}
