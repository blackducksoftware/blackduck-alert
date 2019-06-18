package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;

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
    public void getContentTest() throws Exception {
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic ", null);
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("testProvider")
                                                   .applyTopic("testTopic", "topic")
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        final MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        final ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER, null, contentGroup);
        assertEquals(contentGroup, event.getContent());
    }
}
