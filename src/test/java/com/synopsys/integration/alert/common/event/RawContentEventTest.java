package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RawContentEventTest {
    private static final String TOPIC = "TOPIC";
    private static final String PROVIDER = "PROVIDER";
    private static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String CONTENT = "CONTENT";
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getCreatedAtDateTest() {
        final RawContentEvent event = new RawContentEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        final RawContentEvent event = new RawContentEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(PROVIDER, event.getProvider());
    }

    @Test
    public void getNotificationTypeTest() {
        final RawContentEvent event = new RawContentEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(NOTIFICATION_TYPE, event.getNotificationType());
    }

    @Test
    public void getContentTest() {
        final RawContentEvent event = new RawContentEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(CONTENT, event.getContent());
    }
}
