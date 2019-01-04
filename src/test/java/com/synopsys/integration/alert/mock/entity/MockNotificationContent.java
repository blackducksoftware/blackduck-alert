package com.synopsys.integration.alert.mock.entity;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public class MockNotificationContent extends MockEntityUtil<NotificationContent> {

    private final Date createdAt = new Date();
    private final String provider = "provider";
    private final Date providerCreationTime = new Date();
    private final String notificationType = "notificationType";
    private final String content = "{content: \"content is here...\"}";
    private final Long id = 1L;

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
