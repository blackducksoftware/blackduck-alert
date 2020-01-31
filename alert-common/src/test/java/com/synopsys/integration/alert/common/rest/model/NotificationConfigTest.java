package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NotificationConfigTest {
    private final String id = "test-ID";
    private final String createdAt = "test-createdAt";
    private final String provider = "test-provider";
    private final String providerCreationTime = "test-providerCreationTime";
    private final String notificationType = "test-notificationType";
    private final String content = "test-content";

    //TODO change this into an @beforeeach
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
