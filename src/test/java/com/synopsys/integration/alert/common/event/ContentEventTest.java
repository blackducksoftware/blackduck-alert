package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ContentEventTest {
    private static final String TOPIC = "TOPIC";
    private static final String PROVIDER = "PROVIDER";
    private static final String CONTENT = "CONTENT";
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getCreatedAtDateTest() {
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, CONTENT);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, CONTENT);
        assertEquals(PROVIDER, event.getProvider());
    }

    @Test
    public void getContentTest() {
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, CONTENT);
        assertEquals(CONTENT, event.getContent());
    }
}
