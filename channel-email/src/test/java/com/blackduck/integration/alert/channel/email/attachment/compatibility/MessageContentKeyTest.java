package com.blackduck.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

class MessageContentKeyTest {
    private static final String TOPIC_NAME = "test-topicName";
    private static final String TOPIC_VALUE = "test-topicValue";
    private static final String SUB_TOPIC_NAME = "test-subTopicName";
    private static final String SUB_TOPIC_VALUE = "test-subTopicValue";

    @Test
    void getKeyNoSubTopicTest() {
        MessageContentKey contentKey = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE);
        assertEquals(String.format("%s_%s", TOPIC_NAME, TOPIC_VALUE), contentKey.getKey());
    }

    @Test
    void getKeyNullSubTopicTest() {
        MessageContentKey contentKey1 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, null, SUB_TOPIC_VALUE);
        assertEquals(String.format("%s_%s", TOPIC_NAME, TOPIC_VALUE), contentKey1.getKey());

        MessageContentKey contentKey2 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, null);
        assertEquals(String.format("%s_%s", TOPIC_NAME, TOPIC_VALUE), contentKey2.getKey());
    }

    @Test
    void getKeyWithSubTopicTest() {
        MessageContentKey contentKey = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE);
        assertEquals(String.format("%s_%s_%s_%s", TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE), contentKey.getKey());
    }

    @Test
    void equalsTest() {
        MessageContentKey contentKey1 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE);
        MessageContentKey contentKey2 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE);
        MessageContentKey contentKey3 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE);
        MessageContentKey contentKey4 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE);

        assertEquals(contentKey1, contentKey2);
        assertNotEquals(contentKey1, contentKey3);
        assertEquals(contentKey3, contentKey4);
    }

    @Test
    void getKeyWithLinkableItemTest() {
        MessageContentKey contentKey1 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, null);
        assertEquals(String.format("%s_%s", TOPIC_NAME, TOPIC_VALUE), contentKey1.getKey());

        LinkedHashSet<LinkableItem> subTopics = new LinkedHashSet<>();
        MessageContentKey contentKey2 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, subTopics);
        assertEquals(String.format("%s_%s", TOPIC_NAME, TOPIC_VALUE), contentKey2.getKey());

        LinkableItem linkableItem = new LinkableItem(SUB_TOPIC_NAME, SUB_TOPIC_VALUE);
        subTopics.add(linkableItem);
        MessageContentKey contentKey3 = MessageContentKey.from(TOPIC_NAME, TOPIC_VALUE, subTopics);
        assertEquals(String.format("%s_%s_%s_%s", TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE), contentKey3.getKey());
    }

}
