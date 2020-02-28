package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotificationConfigTest {
    private final String id = "test-ID";
    private final String createdAt = "test-createdAt";
    private final String provider = "test-provider";
    private final String providerCreationTime = "test-providerCreationTime";
    private final String notificationType = "test-notificationType";
    private final String content = "test-content";
    private final Long providerConfigId = 1L;
    private final String providerConfigName = "test-provider-config";
    private NotificationConfig testNotificationConfig;

    @BeforeEach
    public void init() {
        testNotificationConfig = new NotificationConfig(id, createdAt, provider, providerConfigId, providerConfigName, providerCreationTime, notificationType, content);
    }

    @Test
    public void contentTest() {
        String newContent = "new-content";
        testNotificationConfig.setContent(newContent);

        assertEquals(newContent, testNotificationConfig.getContent());
    }

    @Test
    public void createdAtTest() {
        String newCreatedAt = "new-createdAt";
        testNotificationConfig.setCreatedAt(newCreatedAt);

        assertEquals(newCreatedAt, testNotificationConfig.getCreatedAt());
    }

    @Test
    public void providerTest() {
        String newProvider = "new-provider";
        testNotificationConfig.setProvider(newProvider);

        assertEquals(newProvider, testNotificationConfig.getProvider());
    }

    @Test
    public void providerCreationTimeTest() {
        String newProviderCreationTime = "new-providerCreationTime";
        testNotificationConfig.setProviderCreationTime(newProviderCreationTime);

        assertEquals(newProviderCreationTime, testNotificationConfig.getProviderCreationTime());
    }

    @Test
    public void notificationTypeTest() {
        String newNotificationType = "new-notificationType";
        testNotificationConfig.setNotificationType(newNotificationType);

        assertEquals(newNotificationType, testNotificationConfig.getNotificationType());
    }
}
