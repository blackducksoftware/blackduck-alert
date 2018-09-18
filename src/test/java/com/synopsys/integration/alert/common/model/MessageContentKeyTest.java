package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class MessageContentKeyTest {
    @Test
    public void getKeyNoSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final MessageContentKey contentKey = MessageContentKey.from(topicName, topicValue);

        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey.getKey());
    }

    @Test
    public void getKeyNullSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String notNull = "not null";

        final MessageContentKey contentKey1 = MessageContentKey.from(topicName, topicValue, null, notNull);
        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey1.getKey());

        final MessageContentKey contentKey2 = MessageContentKey.from(topicName, topicValue, notNull, null);
        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey2.getKey());
    }

    @Test
    public void getKeyWithSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String subTopicName = "Sub Topic";
        final String subTopicValue = "A Sub Topic";
        final MessageContentKey contentKey = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);

        assertEquals(String.format("%s_%s_%s_%s", topicName, topicValue, subTopicName, subTopicValue), contentKey.getKey());
    }

    @Test
    public void equalsTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String subTopicName = "Sub Topic";
        final String subTopicValue = "A Sub Topic";
        final MessageContentKey contentKey1 = MessageContentKey.from(topicName, topicValue);
        final MessageContentKey contentKey2 = MessageContentKey.from(topicName, topicValue);
        final MessageContentKey contentKey3 = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);
        final MessageContentKey contentKey4 = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);

        assertEquals(contentKey1, contentKey2);
        assertNotEquals(contentKey1, contentKey3);
        assertEquals(contentKey3, contentKey4);
    }
}
