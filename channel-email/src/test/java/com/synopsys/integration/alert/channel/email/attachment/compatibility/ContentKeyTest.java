package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

class ContentKeyTest {
    private static final String PROVIDER_NAME = "test-providerName";
    private static final Long PROVIDER_CONFIG_ID = 1234567890L;
    private static final String TOPIC_NAME = "test-topicName";
    private static final String TOPIC_VALUE = "test-topicValue";
    private static final String SUB_TOPIC_NAME = "test-subTopicName";
    private static final String SUB_TOPIC_VALUE = "test-subTopicValue";
    private static final ItemOperation ITEM_OPERATION = ItemOperation.DELETE;

    @Test
    void noNullDataTest() {
        ContentKey contentKey = ContentKey.of(PROVIDER_NAME, PROVIDER_CONFIG_ID, TOPIC_NAME, TOPIC_VALUE, SUB_TOPIC_NAME, SUB_TOPIC_VALUE, ITEM_OPERATION);
        String contentKeyValue = contentKey.getValue();
        assertEquals(PROVIDER_NAME, contentKey.getProviderName());
        assertEquals(PROVIDER_CONFIG_ID, contentKey.getProviderConfigId());
        assertEquals(TOPIC_NAME, contentKey.getTopicName());
        assertEquals(TOPIC_VALUE, contentKey.getTopicValue());
        assertEquals(SUB_TOPIC_NAME, contentKey.getSubTopicName());
        assertEquals(SUB_TOPIC_VALUE, contentKey.getSubTopicValue());
        assertTrue(contentKeyValue.contains(PROVIDER_NAME));
        assertTrue(contentKeyValue.contains(PROVIDER_CONFIG_ID.toString()));
        assertTrue(contentKeyValue.contains(TOPIC_NAME));
        assertTrue(contentKeyValue.contains(TOPIC_VALUE));
        assertTrue(contentKeyValue.contains(SUB_TOPIC_NAME));
        assertTrue(contentKeyValue.contains(SUB_TOPIC_VALUE));
        assertTrue(contentKeyValue.contains(ITEM_OPERATION.toString()));
        System.out.println(contentKeyValue);
    }

    @Test
    void nullDataTest() {
        List<String> keyParts = new ArrayList<>();
        keyParts.add(PROVIDER_NAME);
        keyParts.add(TOPIC_NAME);
        keyParts.add(TOPIC_VALUE);
        String expectedContentKeyValue = StringUtils.join(keyParts, "_");

        ContentKey contentKey = new ContentKey(PROVIDER_NAME, null, TOPIC_NAME, TOPIC_VALUE, null, SUB_TOPIC_VALUE, null);
        assertEquals(expectedContentKeyValue, contentKey.getValue());
        assertFalse(contentKey.getValue().contains(SUB_TOPIC_VALUE));
    }
}
