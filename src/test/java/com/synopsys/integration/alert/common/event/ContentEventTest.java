package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ContentEventTest {
    private static final String TOPIC = "TOPIC";
    private static final String PROVIDER = "PROVIDER";
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getCreatedAtDateTest() {
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, null);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, null);
        assertEquals(PROVIDER, event.getProvider());
    }

    @Test
    public void getContentTest() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic ", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, new TreeSet<>());
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, content);
        assertEquals(content, event.getContent());
    }
}
