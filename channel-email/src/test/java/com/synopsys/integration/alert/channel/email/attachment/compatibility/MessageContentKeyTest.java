package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class MessageContentKeyTest {
    @Test
    public void getKeyNoSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        MessageContentKey contentKey = MessageContentKey.from(topicName, topicValue);

        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey.getKey());
    }

    @Test
    public void getKeyNullSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String notNull = "not null";

        MessageContentKey contentKey1 = MessageContentKey.from(topicName, topicValue, null, notNull);
        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey1.getKey());

        MessageContentKey contentKey2 = MessageContentKey.from(topicName, topicValue, notNull, null);
        assertEquals(String.format("%s_%s", topicName, topicValue), contentKey2.getKey());
    }

    @Test
    public void getKeyWithSubTopicTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String subTopicName = "Sub Topic";
        final String subTopicValue = "A Sub Topic";
        MessageContentKey contentKey = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);

        assertEquals(String.format("%s_%s_%s_%s", topicName, topicValue, subTopicName, subTopicValue), contentKey.getKey());
    }

    @Test
    public void equalsTest() {
        final String topicName = "Topic";
        final String topicValue = "My Topic";
        final String subTopicName = "Sub Topic";
        final String subTopicValue = "A Sub Topic";
        MessageContentKey contentKey1 = MessageContentKey.from(topicName, topicValue);
        MessageContentKey contentKey2 = MessageContentKey.from(topicName, topicValue);
        MessageContentKey contentKey3 = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);
        MessageContentKey contentKey4 = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);

        assertEquals(contentKey1, contentKey2);
        assertNotEquals(contentKey1, contentKey3);
        assertEquals(contentKey3, contentKey4);
    }

}
