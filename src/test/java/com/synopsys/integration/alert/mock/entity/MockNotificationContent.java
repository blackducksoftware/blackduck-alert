package com.synopsys.integration.alert.mock.entity;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

public class MockNotificationContent extends MockEntityUtil<NotificationEntity> {
    private final Date createdAt;
    private final String provider;
    private final Date providerCreationTime;
    private final String notificationType;
    private final String content;
    private final Long id;
    private final Long providerConfigId;

    public MockNotificationContent() {
        this(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L, 1L);
    }

    public MockNotificationContent(Date createdAt, String provider, Date providerCreationTime, String notificationType, String content, Long id, Long providerConfigId) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.id = id;
        this.providerConfigId = providerConfigId;
    }

    @Override
    public NotificationEntity createEntity() {
        NotificationEntity notificationContent = new NotificationEntity(createdAt, provider, providerConfigId, providerCreationTime, notificationType, content);
        notificationContent.setId(id);
        return notificationContent;
    }

    @Override
    public NotificationEntity createEmptyEntity() {
        return new NotificationEntity();
    }

    @Override
    public String getEntityJson() {
        JsonObject json = new JsonObject();
        // Gson uses locale by default thus I need to use it here
        json.addProperty("createdAt", createdAt.toLocaleString());
        json.addProperty("provider", provider);
        json.addProperty("providerCreationTime", providerCreationTime.toLocaleString());
        json.addProperty("notificationType", notificationType);
        json.addProperty("content", content);
        json.addProperty("id", id);
        json.addProperty("providerConfigId", providerConfigId);
        return json.toString();
    }

    @Override
    public Long getId() {
        return id;
    }

}
