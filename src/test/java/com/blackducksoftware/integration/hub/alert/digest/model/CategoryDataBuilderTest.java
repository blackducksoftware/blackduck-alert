/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.digest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CategoryDataBuilderTest {

    @Test
    public void testBuilderNull() {
        final CategoryDataBuilder categoryDataBuilder = new CategoryDataBuilder();

        assertNull(categoryDataBuilder.getCategoryKey());
        assertNotNull(categoryDataBuilder.getItemList());
        assertTrue(categoryDataBuilder.getItemList().isEmpty());

        final CategoryData categoryData = new CategoryData(null, new LinkedList<>(), 0);
        assertEquals(categoryData, categoryDataBuilder.build());
    }

    @Test
    public void testBuilder() {
        final Map<String, Object> data = new HashMap<>();
        data.put("Key", "Value");
        final ItemData itemData = new ItemData(data);

        final CategoryDataBuilder categoryDataBuilder = new CategoryDataBuilder();

        categoryDataBuilder.addItem(itemData);
        categoryDataBuilder.setCategoryKey("CategoryKey");

        assertEquals("CategoryKey", categoryDataBuilder.getCategoryKey());
        assertNotNull(categoryDataBuilder.getItemList());
        assertFalse(categoryDataBuilder.getItemList().isEmpty());
        assertEquals(itemData, categoryDataBuilder.getItemList().get(0));

        final List<ItemData> dataList = new ArrayList<>();
        dataList.add(itemData);
        CategoryData categoryData = new CategoryData("CategoryKey", dataList, 1);
        assertEquals(categoryData, categoryDataBuilder.build());

        categoryDataBuilder.removeItem(itemData);
        assertNotNull(categoryDataBuilder.getItemList());
        assertTrue(categoryDataBuilder.getItemList().isEmpty());

        categoryData = new CategoryData("CategoryKey", new LinkedList<>(), 0);
        assertEquals(categoryData, categoryDataBuilder.build());
    }
}
