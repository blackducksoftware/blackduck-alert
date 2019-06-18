package com.synopsys.integration.alert.common.message.model;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class ComponentItemTest {

    @Test
    public void testSortedOrder() throws Exception {
        String componentName = "component";
        String subComponent = "1.0.0";

        LinkableItem vuln_1 = new LinkableItem("NEW", "id-1");
        LinkableItem vuln_2 = new LinkableItem("NEW", "id-2");
        LinkableItem vuln_3 = new LinkableItem("UPDATED", "id-3");
        LinkableItem vuln_4 = new LinkableItem("DELETED", "id-4");
        LinkableItem vuln_5 = new LinkableItem("DELETED", "id-5");
        LinkableItem vuln_6 = new LinkableItem("DELETED", "id-6");
        final String category_high = "1 - Severity High";
        final String category_medium = "2 - Severity Medium ";
        final String category_low = "3 - Severity Low";

        ComponentItem componentItem_1 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_low)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of(vuln_1))
                                            .build();

        ComponentItem componentItem_2 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_medium)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.ADD)
                                            .applyAllComponentAttributes(List.of(vuln_2))
                                            .build();

        ComponentItem componentItem_3 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_high)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.UPDATE)
                                            .applyAllComponentAttributes(List.of(vuln_3))
                                            .build();
        ComponentItem componentItem_4 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_low)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyAllComponentAttributes(List.of(vuln_4))
                                            .build();
        ComponentItem componentItem_5 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_medium)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyAllComponentAttributes(List.of(vuln_5))
                                            .build();
        ComponentItem componentItem_6 = new ComponentItem.Builder()
                                            .applyComponentData(componentName, componentName)
                                            .applyCategory(category_high)
                                            .applyNotificationId(1L)
                                            .applyOperation(ItemOperation.DELETE)
                                            .applyAllComponentAttributes(List.of(vuln_6))
                                            .build();

        Collection<ComponentItem> items = List.of(componentItem_2, componentItem_1, componentItem_6, componentItem_5, componentItem_3, componentItem_4).stream()
                                              .sorted(Comparator.comparing(ComponentItem::getCategory)).collect(Collectors.toList());
        Collection<LinkableItem> sortedList = items.stream()
                                                  .map(ComponentItem::getComponentAttributes)
                                                  .flatMap(Set::stream)
                                                  .sorted()
                                                  .collect(Collectors.toList());
        final SortedSet<LinkableItem> combinedItems = new TreeSet<>(sortedList);
        assertFalse(combinedItems.isEmpty());
    }
}
