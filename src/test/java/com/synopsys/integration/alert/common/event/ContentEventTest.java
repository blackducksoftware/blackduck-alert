package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class ContentEventTest {
    private static final String TOPIC = "TOPIC";
    private static final Long PROVIDER_CONFIG_ID = 1l;
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getCreatedAtDateTest() {
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER_CONFIG_ID, null, null);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER_CONFIG_ID, null, null);
        assertEquals(PROVIDER_CONFIG_ID, event.getProviderConfigId());
    }

    @Test
    public void getContentTest() throws Exception {
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic ", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyProject("testTopic", "topic")
                                             .applyProjectVersion(subTopic.getLabel(), subTopic.getValue())
                                             .build();
        MessageContentGroup contentGroup = MessageContentGroup.singleton(content);
        ContentEvent event = new ContentEvent(TOPIC, CREATED_AT, PROVIDER_CONFIG_ID, null, contentGroup);
        assertEquals(contentGroup, event.getContent());
    }

}
