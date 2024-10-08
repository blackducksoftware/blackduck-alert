package com.blackduck.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class MessageContentGroupTest {
    private static final String PROVIDER_NAME = "test-providerName";
    private static final String PROVIDER_CONFIG_NAME = "test-providerConfigName";
    private static final String PROVIDER_URL = "test-providerUrl";
    private static final LinkableItem PROVIDER_LINKABLE_ITEM = new LinkableItem(PROVIDER_NAME, PROVIDER_CONFIG_NAME, PROVIDER_URL);

    private static final String TOPIC_NAME = "test-topicName";
    private static final String TOPIC_VALUE = "test-topicValue";
    private static final String TOPIC_URL = "test-topicUrl";
    private static final LinkableItem TOPIC_LINKABLE_ITEM = new LinkableItem(TOPIC_NAME, TOPIC_VALUE, TOPIC_URL);

    private final ProviderMessageContent.Builder validBuilder = new ProviderMessageContent.Builder();

    @BeforeEach
    void init() {
        validBuilder.applyProvider(PROVIDER_NAME, 12345L, PROVIDER_CONFIG_NAME, PROVIDER_URL)
            .applyTopic(TOPIC_NAME, TOPIC_VALUE, TOPIC_URL);
    }

    @Test
    void addOneValidProviderMessageTest() {
        MessageContentGroup messageContentGroup = new MessageContentGroup();
        assertNull(messageContentGroup.getCommonProvider());
        assertNull(messageContentGroup.getCommonTopic());
        assertEquals(0, messageContentGroup.getSubContent().size());
        assertTrue(messageContentGroup.isEmpty());

        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);

        messageContentGroup.add(providerMessageContent);
        assertEquals(PROVIDER_LINKABLE_ITEM, messageContentGroup.getCommonProvider());
        assertEquals(TOPIC_LINKABLE_ITEM, messageContentGroup.getCommonTopic());
        assertTrue(messageContentGroup.getSubContent().contains(providerMessageContent));
        assertFalse(messageContentGroup.isEmpty());
    }

    @Test
    void addMultipleProviderMessageGoodValueTest() {
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        validBuilder.applyAction(ItemOperation.ADD);
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);
        LinkedHashSet<ProviderMessageContent> providerMessageContents = new LinkedHashSet<>();
        providerMessageContents.add(providerMessageContent1);
        providerMessageContents.add(providerMessageContent2);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.addAll(providerMessageContents);
        assertTrue(messageContentGroup.getSubContent().contains(providerMessageContent1));
        assertTrue(messageContentGroup.getSubContent().contains(providerMessageContent2));
    }

    @Test
    void addMultipleProviderMessageBadValueTest() {
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        validBuilder.applyTopic(TOPIC_NAME, "cause failure", TOPIC_URL);
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent1);
        assertThrows(IllegalArgumentException.class, () -> messageContentGroup.add(providerMessageContent2));
    }

    @Test
    void commonTopicUrlBothNullTest() {
        validBuilder.applyTopicUrl(null);
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        validBuilder.applyTopicUrl(null);
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent1);
        assertEquals(Optional.empty(), messageContentGroup.getCommonTopic().getUrl());
        messageContentGroup.add(providerMessageContent2);
        assertEquals(Optional.empty(), messageContentGroup.getCommonTopic().getUrl());
    }

    @Test
    void commonTopicUrlBothPresentTest() {
        validBuilder.applyTopicUrl(TOPIC_URL);
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        validBuilder.applyTopicUrl("valid url");
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent1);
        assertEquals(TOPIC_URL, messageContentGroup.getCommonTopic().getUrl().orElse(null));
        messageContentGroup.add(providerMessageContent2);
        assertEquals(TOPIC_URL, messageContentGroup.getCommonTopic().getUrl().orElse(null));
    }

    @Test
    void commonTopicUrlSecondNullTest() {
        validBuilder.applyTopicUrl(TOPIC_URL);
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        validBuilder.applyTopicUrl(null);
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent1);
        assertEquals(TOPIC_URL, messageContentGroup.getCommonTopic().getUrl().orElse(null));
        messageContentGroup.add(providerMessageContent2);
        assertEquals(TOPIC_URL, messageContentGroup.getCommonTopic().getUrl().orElse(null));
    }

    @Test
    void commonTopicUrlFirstNullTest() {
        validBuilder.applyTopicUrl(null);
        ProviderMessageContent providerMessageContent1 = assertDoesNotThrow(validBuilder::build);
        String topicUrl = "should see this";
        validBuilder.applyTopicUrl(topicUrl);
        ProviderMessageContent providerMessageContent2 = assertDoesNotThrow(validBuilder::build);

        MessageContentGroup messageContentGroup = new MessageContentGroup();
        messageContentGroup.add(providerMessageContent1);
        assertEquals(Optional.empty(), messageContentGroup.getCommonTopic().getUrl());
        messageContentGroup.add(providerMessageContent2);
        assertEquals(topicUrl, messageContentGroup.getCommonTopic().getUrl().orElse(null));
    }
}
