package com.synopsys.integration.alert.common.message.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

public class LinkableItemTest {

    @Test
    public void testLinkableItemFieldsWithUrl() {
        final String name = "itemName";
        final String value = "itemValue";
        final String url = "item URL";
        LinkableItem item = new LinkableItem(name, value, url);

        assertEquals(name, item.getLabel());
        assertEquals(value, item.getValue());
        assertTrue(item.getUrl().isPresent());
        assertEquals(url, item.getUrl().get());
    }

    @Test
    public void testLinkableItemFieldsMissingUrl() {
        final String name = "itemName";
        final String value = "itemValue";
        LinkableItem item = new LinkableItem(name, value);

        assertEquals(name, item.getLabel());
        assertEquals(value, item.getValue());
        assertFalse(item.getUrl().isPresent());
    }

}
