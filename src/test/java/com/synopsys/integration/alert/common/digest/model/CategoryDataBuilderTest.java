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
package com.synopsys.integration.alert.common.digest.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class CategoryDataBuilderTest {

    @Test
    public void testBuilderNull() {
        final CategoryDataBuilder categoryDataBuilder = new CategoryDataBuilder();

        assertNull(categoryDataBuilder.getCategoryKey());
        assertNotNull(categoryDataBuilder.getItems());
        assertTrue(categoryDataBuilder.getItems().isEmpty());

        final CategoryData categoryData = new CategoryData(null, new LinkedHashSet<>(), 0);
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
        assertNotNull(categoryDataBuilder.getItems());
        assertFalse(categoryDataBuilder.getItems().isEmpty());
        assertEquals(itemData, categoryDataBuilder.getItems().iterator().next());

        final Set<ItemData> dataList = new LinkedHashSet<>();
        dataList.add(itemData);
        CategoryData categoryData = new CategoryData("CategoryKey", dataList, 1);
        assertEquals(categoryData, categoryDataBuilder.build());

        categoryDataBuilder.removeItem(itemData);
        assertNotNull(categoryDataBuilder.getItems());
        assertTrue(categoryDataBuilder.getItems().isEmpty());

        categoryData = new CategoryData("CategoryKey", new LinkedHashSet<>(), 0);
        assertEquals(categoryData, categoryDataBuilder.build());
    }
}
