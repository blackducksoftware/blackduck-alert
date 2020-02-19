package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class ContentEventTest {
    private static final String TOPIC = "TOPIC";
    private static final String PROVIDER = "PROVIDER";
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getCreatedAtDateTest() {
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, null);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, null);
        assertEquals(PROVIDER, event.getProvider());
    }

    @Test
    public void getContentTest() throws Exception {
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic ", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("testProvider", 1L)
                                                   .applyTopic("testTopic", "topic")
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, contentGroup);
        assertEquals(contentGroup, event.getContent());
    }
}
