package com.synopsys.integration.alert.mock.entity;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public class MockNotificationContent extends MockEntityUtil<NotificationContent> {

    private final Date createdAt;
    private final String provider;
    private final Date providerCreationTime;
    private final String notificationType;
    private final String content;
    private final Long id;

    public MockNotificationContent() {
        this(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L);
    }

    public MockNotificationContent(final Date createdAt, final String provider, final Date providerCreationTime, final String notificationType, final String content, final Long id) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;
        this.id = id;
    }

    @Override
    public NotificationContent createEntity() {
        final NotificationContent notificationContent = new NotificationContent(createdAt, provider, providerCreationTime, notificationType, content);
        notificationContent.setId(id);
        return notificationContent;
    }

    @Override
    public NotificationContent createEmptyEntity() {
        return new NotificationContent();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        // Gson uses locale by default thus I need to use it here
        json.addProperty("createdAt", createdAt.toLocaleString());
        json.addProperty("provider", provider);
        json.addProperty("providerCreationTime", providerCreationTime.toLocaleString());
        json.addProperty("notificationType", notificationType);
        json.addProperty("content", content);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public Long getId() {
        return id;
    }
}
