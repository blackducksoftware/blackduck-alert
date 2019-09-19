package com.synopsys.integration.alert.common.message.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class ComponentItemTest {

    @Test
    public void testComparatorEquals() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.NONE)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.NONE)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();
        Comparator<ComponentItem> comparator = ComponentItem.createDefaultComparator();
        int compareResult = comparator.compare(componentItem_1, componentItem_2);
        assertEquals(0, compareResult);

    }

    @Test
    public void testComparatorCategoryDifferent() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyCategory("category B")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        Comparator<ComponentItem> comparator = ComponentItem.createDefaultComparator();
        int compareResult = comparator.compare(componentItem_1, componentItem_2);
        assertEquals(1, compareResult);

    }

    @Test
    public void testComparatorPriorityDifferent() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.HIGH)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        Comparator<ComponentItem> comparator = ComponentItem.createDefaultComparator();
        int compareResult = comparator.compare(componentItem_1, componentItem_2);
        assertEquals(1, compareResult);

    }

    @Test
    public void testComparatorMissingPriority() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.LOW)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        Comparator<ComponentItem> comparator = ComponentItem.createDefaultComparator();
        int compareResult = comparator.compare(componentItem_1, componentItem_2);
        assertTrue(compareResult > 0);

    }

    @Test
    public void testComparatorMissingSubComponent() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyPriority(ComponentItemPriority.HIGH)
                                            .applyCategory("category A")
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of())
                                            .build();

        Comparator<ComponentItem> comparator = ComponentItem.createDefaultComparator();
        int compareResult = comparator.compare(componentItem_1, componentItem_2);
        assertTrue(compareResult > 0);

        int reversedCompareResult = comparator.compare(componentItem_2, componentItem_1);
        assertTrue(reversedCompareResult < 0);
    }

    @Test
    public void testSortedOrder() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";
        String categoryItemName = "category";
        String categoryGroup = "group 1";

        LinkableItem vuln_1 = new LinkableItem("NEW", "id-1");
        LinkableItem vuln_2 = new LinkableItem("NEW", "id-2");
        LinkableItem vuln_3 = new LinkableItem("UPDATED", "id-3");
        LinkableItem vuln_4 = new LinkableItem("DELETED", "id-4");
        LinkableItem vuln_5 = new LinkableItem("DELETED", "id-5");
        LinkableItem vuln_6 = new LinkableItem("DELETED", "id-6");
        final String category = "vulnerability";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyAllComponentAttributes(List.of(vuln_1))
                                            .applyNotificationId(1L)
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyPriority(ComponentItemPriority.LOW)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyAllComponentAttributes(List.of(vuln_2))
                                            .applyNotificationId(1L)
                                            .build();

        ComponentItem componentItem_3 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyPriority(ComponentItemPriority.HIGH)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyAllComponentAttributes(List.of(vuln_3))
                                            .applyNotificationId(1L)
                                            .build();
        ComponentItem componentItem_4 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyPriority(ComponentItemPriority.MEDIUM)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyAllComponentAttributes(List.of(vuln_4))
                                            .applyNotificationId(1L)
                                            .build();
        ComponentItem componentItem_5 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyPriority(ComponentItemPriority.LOW)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyAllComponentAttributes(List.of(vuln_5))
                                            .applyNotificationId(1L)
                                            .build();
        ComponentItem componentItem_6 = new ComponentItem.Builder()
                                            .applyCategory(category)
                                            .applyOperation(ItemOperation.UPDATE)
                                            .applyPriority(ComponentItemPriority.HIGH)
                                            .applyComponentData(componentName, componentName)
                                            .applySubComponent("subComponent", subComponent)
                                            .applyCategoryItem(categoryItemName, categoryItemName)
                                            .applyCategoryGroupingAttribute(categoryGroup, categoryGroup)
                                            .applyNotificationId(1L)
                                            .applyAllComponentAttributes(List.of(vuln_6))
                                            .build();

        Collection<ComponentItem> expected = List.of(componentItem_1, componentItem_2, componentItem_3, componentItem_4, componentItem_5, componentItem_6);

        Collection<ComponentItem> items = List.of(componentItem_2, componentItem_1, componentItem_6, componentItem_5, componentItem_3, componentItem_4).stream()
                                              .sorted(ComponentItem.createDefaultComparator()).collect(Collectors.toList());
        assertEquals(expected, items);
    }

}
