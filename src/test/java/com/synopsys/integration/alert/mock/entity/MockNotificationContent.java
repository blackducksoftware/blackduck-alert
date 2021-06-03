package com.synopsys.integration.alert.mock.entity;

import java.time.OffsetDateTime;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class MockNotificationContent extends MockEntityUtil<NotificationEntity> {
    private static final String DEFAULT_NOTIFICATION_TYPE_NAME = NotificationType.BOM_EDIT.name();
    private static final String BOM_EDIT_CONTENT = "{"
                                                       + "\"type\":\"" + DEFAULT_NOTIFICATION_TYPE_NAME + "\","
                                                       + "\"content\": {"
                                                       + "\"projectVersion\": \"https://test\","
                                                       + "\"bomComponent\": \"https://test\","
                                                       + "\"componentName\": \"test\","
                                                       + "\"componentVersionName\": \"test\""
                                                       + "}"
                                                       + "}";

    private final OffsetDateTime createdAt;
    private final String provider;
    private final OffsetDateTime providerCreationTime;
    private final String notificationType;
    private final String content;
    private final Long id;
    private Long providerConfigId;

    public MockNotificationContent() {
        this(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), DEFAULT_NOTIFICATION_TYPE_NAME, BOM_EDIT_CONTENT, 1L, 1L);
    }

    public MockNotificationContent(OffsetDateTime createdAt, String provider, OffsetDateTime providerCreationTime, String notificationType, String content, Long id, Long providerConfigId) {
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
        NotificationEntity notificationContent = new NotificationEntity(createdAt, provider, providerConfigId, providerCreationTime, notificationType, content, false);
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
        json.addProperty("createdAt", createdAt.toString());
        json.addProperty("provider", provider);
        json.addProperty("providerCreationTime", providerCreationTime.toString());
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

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public void setProviderConfigId(Long providerConfigId) {
        this.providerConfigId = providerConfigId;
    }

}
