package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

class ComponentItemTest {
    private static final String CATEGORY = "test-category";

    private static final String COMPONENT_LABEL = "test-componentLabel";
    private static final String COMPONENT_VALUE = "test-componentValue";
    private static final String COMPONENT_URL = "test-componentUrl";
    private static final LinkableItem COMPONENT_LINKABLE_ITEM = new LinkableItem(COMPONENT_LABEL, COMPONENT_VALUE, COMPONENT_URL);

    private static final String SUB_COMPONENT_LABEL = "test-subComponentLabel";
    private static final String SUB_COMPONENT_VALUE = "test-subComponentValue";
    private static final String SUB_COMPONENT_URL = "test-subComponentUrl";
    private static final LinkableItem SUB_COMPONENT_LINKABLE_ITEM = new LinkableItem(SUB_COMPONENT_LABEL, SUB_COMPONENT_VALUE, SUB_COMPONENT_URL);

    private static final String CATEGORY_ITEM_LABEL = "test-categoryItemLabel";
    private static final String CATEGORY_ITEM_VALUE = "test-categoryItemValue";
    private static final String CATEGORY_ITEM_URL = "test-categoryItemUrl";
    private static final LinkableItem CATEGORY_ITEM_LINKABLE_ITEM = new LinkableItem(CATEGORY_ITEM_LABEL, CATEGORY_ITEM_VALUE, CATEGORY_ITEM_URL);

    private static final String CATEGORY_ITEM_ATTR_LABEL = "test-categoryGroupingAttributeLabel";
    private static final LinkableItem SUB_CATEGORY_ITEM_LINKABLE_ITEM = new LinkableItem(CATEGORY_ITEM_ATTR_LABEL, "test-categoryGroupingAttributeValue");

    private static final ItemOperation ITEM_OPERATION = ItemOperation.DELETE;
    private static final ComponentItemPriority COMPONENT_ITEM_PRIORITY = ComponentItemPriority.MEDIUM;

    private static final String COMPONENT_CALLBACK_URL = "test-componentCallbackUrl";
    private static final ProviderKey PROVIDER_KEY = new ProviderKey("universalKey", "displayName");
    private static final ComponentItemCallbackInfo CALL_BACK_INFO = new ComponentItemCallbackInfo(COMPONENT_CALLBACK_URL, PROVIDER_KEY, "test-componentCallbackNotificationType");

    private final ComponentItem.Builder validBuilder = new ComponentItem.Builder();

    @BeforeEach
    void init() {
        validBuilder.applyCategory(CATEGORY)
            .applyOperation(ITEM_OPERATION)
            .applyComponentData(COMPONENT_LINKABLE_ITEM)
            .applyCategoryItem(CATEGORY_ITEM_LINKABLE_ITEM);
    }

    @Test
    void validateRequiredFieldsPresentTest() {
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertEquals(CATEGORY, componentItem.getCategory());
        assertEquals(ITEM_OPERATION, componentItem.getOperation());
        assertEquals(COMPONENT_LINKABLE_ITEM, componentItem.getComponent());
        assertEquals(CATEGORY_ITEM_LINKABLE_ITEM, componentItem.getCategoryItem());
    }

    @Test
    void validateRequiredFieldsMissingTest() {
        // Test category null
        ComponentItem.Builder builder = new ComponentItem.Builder();
        assertThrows(AlertException.class, builder::build);

        // Test operation null
        builder.applyCategory(CATEGORY);
        assertThrows(AlertException.class, builder::build);

        // Test componentLabel null
        builder.applyOperation(ITEM_OPERATION);
        assertThrows(AlertException.class, builder::build);

        // Test component null
        builder.applyComponentData(null);
        assertThrows(AlertException.class, builder::build);

        // Test componentValue null
        builder.applyComponentData(COMPONENT_LABEL, null);
        assertThrows(AlertException.class, builder::build);

        // Test categoryItem null
        builder.applyCategoryItem(null);
        assertThrows(AlertException.class, builder::build);

        // Test categoryItemLabel null
        builder.applyComponentData(COMPONENT_LABEL, COMPONENT_VALUE);
        assertThrows(AlertException.class, builder::build);

        // Test categoryItemValue null
        builder.applyCategoryItem(CATEGORY_ITEM_LABEL, null);
        AlertException alertException = assertThrows(AlertException.class, builder::build);
        assertEquals("Missing required field(s)", alertException.getMessage());
    }

    @Test
    void validateSubcomponentTest() {
        validBuilder.applySubComponent(null);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getSubComponent().isEmpty());

        validBuilder.applySubComponent(SUB_COMPONENT_LABEL, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getSubComponent().isEmpty());

        validBuilder.applySubComponent(null, SUB_COMPONENT_VALUE);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getSubComponent().isEmpty());

        validBuilder.applySubComponent(SUB_COMPONENT_LINKABLE_ITEM);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getSubComponent().isPresent());
        assertEquals(Optional.of(SUB_COMPONENT_LINKABLE_ITEM), componentItem.getSubComponent());
    }

    @Test
    void validateSubCategoryTest() {
        validBuilder.applyCategoryGroupingAttribute(null);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCategoryGroupingAttribute().isEmpty());

        validBuilder.applyCategoryGroupingAttribute(null, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCategoryGroupingAttribute().isEmpty());

        validBuilder.applyCategoryGroupingAttribute(CATEGORY_ITEM_ATTR_LABEL, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCategoryGroupingAttribute().isEmpty());

        validBuilder.applyCategoryGroupingAttribute(SUB_CATEGORY_ITEM_LINKABLE_ITEM);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCategoryGroupingAttribute().isPresent());
        assertEquals(Optional.of(SUB_CATEGORY_ITEM_LINKABLE_ITEM), componentItem.getCategoryGroupingAttribute());
    }

    @Test
    void validateCallBackTest() {
        validBuilder.applyComponentItemCallbackInfo(null);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCallbackInfo().isEmpty());

        validBuilder.applyComponentItemCallbackInfo(null, null, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCallbackInfo().isEmpty());

        validBuilder.applyComponentItemCallbackInfo(COMPONENT_CALLBACK_URL, null, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCallbackInfo().isEmpty());

        validBuilder.applyComponentItemCallbackInfo(COMPONENT_CALLBACK_URL, PROVIDER_KEY, null);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCallbackInfo().isEmpty());

        validBuilder.applyComponentItemCallbackInfo(CALL_BACK_INFO);
        componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.getCallbackInfo().isPresent());
        assertEquals(Optional.of(CALL_BACK_INFO), componentItem.getCallbackInfo());
    }

    @Test
    void validatePriorityTest() {
        validBuilder.applyPriority(COMPONENT_ITEM_PRIORITY);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertEquals(COMPONENT_ITEM_PRIORITY, componentItem.getPriority());
    }

    @Test
    void validateCollapseOnCategoryTest() {
        validBuilder.applyCollapseOnCategory(true);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        assertTrue(componentItem.collapseOnCategory());
        assertTrue(componentItem.getCollapseOnCategory());
    }

    @Test
    void validateNotificationIdTest() {
        Set<Long> expectedNotificationIds = new LinkedHashSet<>();
        expectedNotificationIds.add(123456789L);
        expectedNotificationIds.add(987654321L);
        validBuilder.applyNotificationIds(expectedNotificationIds);

        long singleNotificationId = 1234554321L;
        validBuilder.applyNotificationId(singleNotificationId);

        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        Set<Long> actualNotificationIds = componentItem.getNotificationIds();

        assertTrue(actualNotificationIds.contains(singleNotificationId));
        expectedNotificationIds.forEach(expected -> assertTrue(actualNotificationIds.contains(expected)));
    }

    @Test
    void validateComponentAttributesTest() {
        LinkableItem linkableItem1 = new LinkableItem("label1", "value1");
        LinkableItem linkableItem2 = new LinkableItem("label2", "value2");
        LinkedHashSet<LinkableItem> expectedComponentAttributes = new LinkedHashSet<>();
        expectedComponentAttributes.add(linkableItem1);
        expectedComponentAttributes.add(linkableItem2);
        validBuilder.applyAllComponentAttributes(expectedComponentAttributes);

        LinkableItem singleComponentAttribute = new LinkableItem("label3", "value3");
        validBuilder.applyComponentAttribute(singleComponentAttribute);

        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        LinkedHashSet<LinkableItem> actualComponentAttributes = componentItem.getComponentAttributes();

        assertTrue(actualComponentAttributes.contains(singleComponentAttribute));
        expectedComponentAttributes.forEach(expected -> assertTrue(actualComponentAttributes.contains(expected)));
    }

    @Test
    void validateEqualsTest() {
        ComponentItem componentItem1 = assertDoesNotThrow(validBuilder::build);
        ComponentItem componentItem2 = assertDoesNotThrow(validBuilder::build);

        assertEquals(componentItem1, componentItem2);
        assertEquals(componentItem1.hashCode(), componentItem2.hashCode());
    }

    @Test
    void validateCompareOptionalItemsTest() {
        ComponentItem.Builder builderNoSubComponent = validBuilder;
        ComponentItem componentItemNoSubComponent1 = assertDoesNotThrow(builderNoSubComponent::build);
        ComponentItem componentItemNoSubComponent2 = assertDoesNotThrow(builderNoSubComponent::build);

        ComponentItem.Builder builderWithSubComponent = validBuilder.applySubComponent(SUB_COMPONENT_LINKABLE_ITEM);
        ComponentItem componentItemWithSubComponent1 = assertDoesNotThrow(builderWithSubComponent::build);
        ComponentItem componentItemWithSubComponent2 = assertDoesNotThrow(builderWithSubComponent::build);

        Comparator<ComponentItem> defaultComparator = ComponentItem.createDefaultComparator();
        assertEquals(0, defaultComparator.compare(componentItemNoSubComponent1, componentItemNoSubComponent2));
        assertEquals(0, defaultComparator.compare(componentItemWithSubComponent1, componentItemWithSubComponent2));
        assertEquals(-1, defaultComparator.compare(componentItemNoSubComponent1, componentItemWithSubComponent1));
        assertEquals(1, defaultComparator.compare(componentItemWithSubComponent1, componentItemNoSubComponent1));
    }

    @Test
    void validateCreateKeyCollapseTrueTest() {
        validBuilder.applyCollapseOnCategory(true);
        validBuilder.applySubComponent(SUB_COMPONENT_LINKABLE_ITEM);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        String key = componentItem.createKey(false, false);
        assertTrue(key.contains(CATEGORY));
        assertTrue(key.contains(COMPONENT_LABEL));
        assertTrue(key.contains(COMPONENT_VALUE));
        assertTrue(key.contains(COMPONENT_URL));
        assertTrue(key.contains(SUB_COMPONENT_LABEL));
        assertTrue(key.contains(SUB_COMPONENT_VALUE));
        assertTrue(key.contains(SUB_COMPONENT_URL));
        assertFalse(key.contains(ITEM_OPERATION.toString()));
        assertFalse(key.contains(CATEGORY_ITEM_VALUE));
        assertFalse(key.contains(CATEGORY_ITEM_LABEL));
        assertFalse(key.contains(CATEGORY_ITEM_URL));

        validBuilder.applyCollapseOnCategory(true);
        componentItem = assertDoesNotThrow(validBuilder::build);
        key = componentItem.createKey(false, true);
        assertTrue(key.contains(CATEGORY_ITEM_VALUE));
        assertTrue(key.contains(CATEGORY_ITEM_LABEL));
        assertTrue(key.contains(CATEGORY_ITEM_URL));

        validBuilder.applyCollapseOnCategory(true);
        componentItem = assertDoesNotThrow(validBuilder::build);
        key = componentItem.createKey();
        assertTrue(key.contains(ITEM_OPERATION.toString()));
    }

    @Test
    void validateCreateKeyCollapseFalseTest() {
        validBuilder.applyCollapseOnCategory(false);
        ComponentItem componentItem = assertDoesNotThrow(validBuilder::build);
        String key = componentItem.createKey(true, false);
        assertTrue(key.contains(ITEM_OPERATION.toString()));

        validBuilder.applyCollapseOnCategory(false);
        componentItem = assertDoesNotThrow(validBuilder::build);
        key = componentItem.createKey(true, true);
        assertTrue(key.contains(ITEM_OPERATION.toString()));

        validBuilder.applyCollapseOnCategory(false);
        componentItem = assertDoesNotThrow(validBuilder::build);
        key = componentItem.createKey(false, true);
        assertTrue(key.contains(CATEGORY_ITEM_VALUE));
        assertTrue(key.contains(CATEGORY_ITEM_LABEL));
        assertTrue(key.contains(CATEGORY_ITEM_URL));
    }
}
