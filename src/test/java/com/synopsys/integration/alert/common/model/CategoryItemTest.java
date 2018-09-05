package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class CategoryItemTest {

    @Test
    public void testCategoryItemFields() {

        final CategoryKey categoryKey = CategoryKey.from("categoryKey");
        final ItemOperation operation = ItemOperation.ADD;
        final List<LinkableItem> itemList = Collections.emptyList();
        final CategoryItem categoryItem = new CategoryItem(categoryKey, operation, itemList);
        assertEquals(categoryKey, categoryItem.getCategoryKey());
        assertEquals(operation, categoryItem.getOperation());
        assertEquals(itemList, categoryItem.getItemList());
    }
}
