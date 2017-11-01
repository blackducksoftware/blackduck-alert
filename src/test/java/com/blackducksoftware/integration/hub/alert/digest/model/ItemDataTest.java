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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ItemDataTest {

    @Test
    public void testDataNull() {
        final ItemData itemData = new ItemData(null);

        assertNull(itemData.getDataSet());

        assertEquals("{\"dataSet\":null}", itemData.toString());
    }

    @Test
    public void testData() {
        final Map<String, Object> data = new HashMap<>();
        data.put("Key", "Value");
        final ItemData itemData = new ItemData(data);

        assertNotNull(itemData.getDataSet());
        assertFalse(itemData.getDataSet().isEmpty());
        assertEquals("Value", itemData.getDataSet().get("Key"));

        assertEquals("{\"dataSet\":{Key=Value}}", itemData.toString());
    }

}
