package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.datastructure.SetMap;

public class ProviderMessageContentTest {
    public static final String PROVIDER_NAME = "test-providerName";
    public static final String PROVIDER_CONFIG_NAME = "test-providerConfigName";
    public static final String PROVIDER_URL = "test-providerUrl";
    public static final LinkableItem PROVIDER_LINKABLE_ITEM = new LinkableItem(PROVIDER_NAME, PROVIDER_CONFIG_NAME, PROVIDER_URL);

    public static final String TOPIC_NAME = "test-topicName";
    public static final String TOPIC_VALUE = "test-topicValue";
    public static final String TOPIC_URL = "test-topicUrl";
    public static final LinkableItem TOPIC_LINKABLE_ITEM = new LinkableItem(TOPIC_NAME, TOPIC_VALUE, TOPIC_URL);

    public static final String SUB_TOPIC_NAME = "test-subTopicName";
    public static final String SUB_TOPIC_VALUE = "test-subTopicValue";
    public static final String SUB_TOPIC_URL = "test-subTopicUrl";
    public static final LinkableItem SUB_TOPIC_LINKABLE_ITEM = new LinkableItem(SUB_TOPIC_NAME, SUB_TOPIC_VALUE, SUB_TOPIC_URL);

    public static final Long PROVIDER_CONFIG_ID = 1234567890L;
    public static final ItemOperation ITEM_OPERATION_1 = ItemOperation.ADD;
    public static final ItemOperation ITEM_OPERATION_2 = ItemOperation.UPDATE;
    public static final Long NOTIFICATION_ID = 9876543210L;

    public final ProviderMessageContent.Builder validBuilder = new ProviderMessageContent.Builder();
    public final ComponentItem.Builder validComponentItemBuilder1 = new ComponentItem.Builder();
    public final ComponentItem.Builder validComponentItemBuilder2 = new ComponentItem.Builder();

    @BeforeEach
    public void init() {
        validBuilder.applyProvider(PROVIDER_NAME, PROVIDER_CONFIG_ID, PROVIDER_CONFIG_NAME, PROVIDER_URL)
            .applyTopic(TOPIC_NAME, TOPIC_VALUE, TOPIC_URL);

        validComponentItemBuilder1.applyCategory("category1")
            .applyOperation(ITEM_OPERATION_1)
            .applyComponentData("label1", "value1")
            .applyCategoryItem("label1", "value1");

        validComponentItemBuilder2.applyCategory("category2")
            .applyOperation(ITEM_OPERATION_2)
            .applyComponentData("label2", "value2")
            .applyCategoryItem("label2", "value2");
    }

    @Test
    public void validateRequiredFieldsPresentTest() {
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(PROVIDER_LINKABLE_ITEM, providerMessageContent.getProvider());
        assertEquals(PROVIDER_CONFIG_ID, providerMessageContent.getProviderConfigId());
        assertEquals(TOPIC_LINKABLE_ITEM, providerMessageContent.getTopic());

        String contentKeyValue = providerMessageContent.getContentKey().getValue();
        assertTrue(contentKeyValue.contains(PROVIDER_NAME));
        assertTrue(contentKeyValue.contains(PROVIDER_CONFIG_ID.toString()));
        assertTrue(contentKeyValue.contains(TOPIC_NAME));
        assertTrue(contentKeyValue.contains(TOPIC_VALUE));
    }

    @Test
    public void validateRequiredFieldsMissingTest() {
        ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder();
        assertEquals("__", builder.getCurrentContentKey().getValue());

        // Test providerName null
        builder.applyProvider(null, null, null);
        assertThrows(AlertException.class, builder::build);

        // Test providerConfigId null
        builder.applyProvider(PROVIDER_NAME, null, null);
        assertThrows(AlertException.class, builder::build);

        // Test providerConfigName null
        builder.applyProvider(PROVIDER_NAME, PROVIDER_CONFIG_ID, null);
        assertThrows(AlertException.class, builder::build);

        // Test topicName null
        builder.applyProvider(PROVIDER_NAME, PROVIDER_CONFIG_ID, PROVIDER_CONFIG_NAME);
        assertThrows(AlertException.class, builder::build);

        // Test topicValue null
        builder.applyTopic(TOPIC_NAME, null);
        AlertException alertException = assertThrows(AlertException.class, builder::build);
        assertEquals("Missing required field(s)", alertException.getMessage());
    }

    @Test
    public void validateSubTopicTest() {
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.empty(), providerMessageContent.getSubTopic());

        validBuilder.applySubTopic(SUB_TOPIC_NAME, null, null);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.empty(), providerMessageContent.getSubTopic());

        validBuilder.applySubTopic(null, SUB_TOPIC_VALUE, null);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.empty(), providerMessageContent.getSubTopic());

        validBuilder.applySubTopic(SUB_TOPIC_NAME, SUB_TOPIC_VALUE, null);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(SUB_TOPIC_NAME, providerMessageContent.getSubTopic().get().getLabel());
        assertEquals(SUB_TOPIC_VALUE, providerMessageContent.getSubTopic().get().getValue());
        assertEquals(Optional.empty(), providerMessageContent.getSubTopic().get().getUrl());

        validBuilder.applySubTopic(SUB_TOPIC_NAME, SUB_TOPIC_VALUE, SUB_TOPIC_URL);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.of(SUB_TOPIC_LINKABLE_ITEM), providerMessageContent.getSubTopic());
        String contentKeyValue = providerMessageContent.getContentKey().getValue();
        assertEquals(validBuilder.getCurrentContentKey().getValue(), contentKeyValue);
        assertTrue(contentKeyValue.contains(SUB_TOPIC_NAME));
        assertTrue(contentKeyValue.contains(SUB_TOPIC_VALUE));
    }

    @Test
    public void validateActionTest() {
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.empty(), providerMessageContent.getAction());
        assertFalse(providerMessageContent.getContentKey().getValue().contains(ITEM_OPERATION_1.toString()));
        assertFalse(validBuilder.getCurrentContentKey().getValue().contains(ITEM_OPERATION_1.toString()));

        validBuilder.applyAction(ITEM_OPERATION_1);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.of(ITEM_OPERATION_1), providerMessageContent.getAction());
        assertTrue(providerMessageContent.getContentKey().getValue().contains(ITEM_OPERATION_1.toString()));
        assertTrue(validBuilder.getCurrentContentKey().getValue().contains(ITEM_OPERATION_1.toString()));
    }

    @Test
    public void validateNotificationIdTest() {
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.empty(), providerMessageContent.getNotificationId());

        validBuilder.applyNotificationId(NOTIFICATION_ID);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(Optional.of(NOTIFICATION_ID), providerMessageContent.getNotificationId());
    }

    @Test
    public void validateComponentItemTest() {
        ComponentItem componentItem1 = assertDoesNotThrow(validComponentItemBuilder1::build);
        ComponentItem componentItem2 = assertDoesNotThrow(validComponentItemBuilder2::build);
        Set<ComponentItem> componentItems = new LinkedHashSet<>();
        componentItems.add(componentItem2);

        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.getComponentItems().isEmpty());

        validBuilder.applyComponentItem(componentItem1);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.getComponentItems().contains(componentItem1));

        validBuilder.applyAllComponentItems(componentItems);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.getComponentItems().contains(componentItem2));
    }

    @Test
    public void validateProviderCreationTimeTest() {
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertNull(providerMessageContent.getProviderCreationTime());

        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        validBuilder.applyProviderCreationTime(offsetDateTime);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(offsetDateTime, providerMessageContent.getProviderCreationTime());
    }

    @Test
    public void validateApplyEarliestProviderCreationTimeTest() {
        OffsetDateTime currentOffsetDateTime = OffsetDateTime.now();
        OffsetDateTime pastOffsetDateTime = OffsetDateTime.of(1966, 9, 8, 0, 0, 1, 1, ZoneOffset.of("-04:00"));
        OffsetDateTime futureOffsetDateTime = OffsetDateTime.of(2233, 3, 22, 0, 0, 1, 1, ZoneOffset.of("-04:00"));

        validBuilder.applyEarliestProviderCreationTime(currentOffsetDateTime);
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(currentOffsetDateTime, providerMessageContent.getProviderCreationTime());

        validBuilder.applyEarliestProviderCreationTime(pastOffsetDateTime);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(pastOffsetDateTime, providerMessageContent.getProviderCreationTime());

        validBuilder.applyEarliestProviderCreationTime(futureOffsetDateTime);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertEquals(pastOffsetDateTime, providerMessageContent.getProviderCreationTime());
    }

    @Test
    public void validateIsTopLevelActionOnlyTest() {
        // Test action null
        validBuilder.applyAction(null)
            .applyNotificationId(null);
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertFalse(providerMessageContent.isTopLevelActionOnly());

        // Test notificationId null
        validBuilder.applyAction(ITEM_OPERATION_1)
            .applyNotificationId(null);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertFalse(providerMessageContent.isTopLevelActionOnly());

        // Test componentItems empty
        validBuilder.applyAction(ITEM_OPERATION_1)
            .applyNotificationId(NOTIFICATION_ID);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.getComponentItems().isEmpty());
        assertTrue(providerMessageContent.isTopLevelActionOnly());

        // Test componentItems NOT empty
        ComponentItem componentItem = assertDoesNotThrow(validComponentItemBuilder1::build);
        validBuilder.applyAction(ITEM_OPERATION_1)
            .applyNotificationId(NOTIFICATION_ID)
            .applyComponentItem(componentItem);
        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertFalse(providerMessageContent.getComponentItems().isEmpty());
        assertFalse(providerMessageContent.isTopLevelActionOnly());
    }

    @Test
    public void validateGroupRelatedComponentItemsFalseTest() {
        boolean includeOperation = false;
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.groupRelatedComponentItems().isEmpty());
        assertTrue(providerMessageContent.groupRelatedComponentItems(includeOperation).isEmpty());

        ComponentItem componentItem1 = assertDoesNotThrow(validComponentItemBuilder1::build);
        String componentItem1Key = componentItem1.createKey(includeOperation, false);
        ComponentItem componentItem2 = assertDoesNotThrow(validComponentItemBuilder2::build);
        String componentItem2Key = componentItem2.createKey(includeOperation, false);
        validBuilder.applyComponentItem(componentItem1)
            .applyComponentItem(componentItem2);

        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        SetMap<String, ComponentItem> relatedComponentItems = providerMessageContent.groupRelatedComponentItems(includeOperation);
        assertFalse(relatedComponentItems.isEmpty());
        assertTrue(relatedComponentItems.containsKey(componentItem1Key));
        assertEquals(componentItem1, relatedComponentItems.getMap().get(componentItem1Key).toArray()[0]);
        assertTrue(relatedComponentItems.containsKey(componentItem2Key));
        assertEquals(componentItem2, relatedComponentItems.getMap().get(componentItem2Key).toArray()[0]);
    }

    @Test
    public void validateGroupRelatedComponentItemsTrueTest() {
        boolean includeOperation = true;
        ProviderMessageContent providerMessageContent = assertDoesNotThrow(validBuilder::build);
        assertTrue(providerMessageContent.groupRelatedComponentItems().isEmpty());
        assertTrue(providerMessageContent.groupRelatedComponentItems(includeOperation).isEmpty());

        ComponentItem componentItem1 = assertDoesNotThrow(validComponentItemBuilder1::build);
        String componentItem1Key = componentItem1.createKey(includeOperation, false);
        ComponentItem componentItem2 = assertDoesNotThrow(validComponentItemBuilder2::build);
        String componentItem2Key = componentItem2.createKey(includeOperation, false);
        validBuilder.applyComponentItem(componentItem1)
            .applyComponentItem(componentItem2);

        providerMessageContent = assertDoesNotThrow(validBuilder::build);
        SetMap<String, ComponentItem> relatedComponentItems = providerMessageContent.groupRelatedComponentItems(includeOperation);
        assertFalse(relatedComponentItems.isEmpty());
        assertTrue(relatedComponentItems.containsKey(componentItem1Key));
        assertEquals(componentItem1, relatedComponentItems.getMap().get(componentItem1Key).toArray()[0]);
        assertTrue(relatedComponentItems.containsKey(componentItem2Key));
        assertEquals(componentItem2, relatedComponentItems.getMap().get(componentItem2Key).toArray()[0]);
    }
}
