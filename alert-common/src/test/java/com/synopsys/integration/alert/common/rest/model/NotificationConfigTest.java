package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NotificationConfigTest {

    String id = "test-ID";
    String createdAt = "test-createdAt";
    String provider = "test-provider";
    String providerCreationTime = "test-providerCreationTime";
    String notificationType = "test-notificationType";
    String content = "test-content";

    public NotificationConfig setupNotificationConfig() {
        NotificationConfig testNotificationConfig = new NotificationConfig(id, createdAt, provider, providerCreationTime, notificationType, content);
        return testNotificationConfig;
    }

    @Test
    public void contentTest() {
        String newContent = "new-content";
        NotificationConfig testNotificationConfig = setupNotificationConfig();
        testNotificationConfig.setContent(newContent);

        assertEquals(newContent, testNotificationConfig.getContent());
    }

    @Test
    public void createdAtTest() {
        String newCreatedAt = "new-createdAt";
        NotificationConfig testNotificationConfig = setupNotificationConfig();
        testNotificationConfig.setCreatedAt(newCreatedAt);

        assertEquals(newCreatedAt, testNotificationConfig.getCreatedAt());
    }

    @Test
    public void providerTest() {
        String newProvider = "new-provider";
        NotificationConfig testNotificationConfig = setupNotificationConfig();
        testNotificationConfig.setProvider(newProvider);

        assertEquals(newProvider, testNotificationConfig.getProvider());
    }

    @Test
    public void providerCreationTimeTest() {
        String newProviderCreationTime = "new-providerCreationTime";
        NotificationConfig testNotificationConfig = setupNotificationConfig();
        testNotificationConfig.setProviderCreationTime(newProviderCreationTime);

        assertEquals(newProviderCreationTime, testNotificationConfig.getProviderCreationTime());
    }

    @Test
    public void notificationTypeTest() {
        String newNotificationType = "new-notificationType";
        NotificationConfig testNotificationConfig = setupNotificationConfig();
        testNotificationConfig.setNotificationType(newNotificationType);

        assertEquals(newNotificationType, testNotificationConfig.getNotificationType());
    }
}
