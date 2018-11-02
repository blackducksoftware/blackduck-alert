package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class CategoryItemTest {

    @Test
    public void testCategoryItemFields() {
        final CategoryKey categoryKey = CategoryKey.from("categoryKey");
        final ItemOperation operation = ItemOperation.ADD;
        final SortedSet<LinkableItem> items = new TreeSet<>();
        final Long notificationId = 10L;
        final CategoryItem categoryItem = new CategoryItem(categoryKey, operation, notificationId, items);
        assertEquals(categoryKey, categoryItem.getCategoryKey());
        assertEquals(operation, categoryItem.getOperation());
        assertEquals(notificationId, categoryItem.getNotificationId());
        assertEquals(items, categoryItem.getItems());
    }
}
