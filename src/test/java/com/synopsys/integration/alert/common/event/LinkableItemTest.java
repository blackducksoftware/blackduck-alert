package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinkableItemTest {

    @Test
    public void testLinkableItemFieldsWithUrl() {
        final String name = "itemName";
        final String value = "itemValue";
        final String url = "item URL";
        final LinkableItem item = new LinkableItem(name, value, url);

        assertEquals(name, item.getName());
        assertEquals(value, item.getValue());
        assertTrue(item.getUrl().isPresent());
        assertEquals(url, item.getUrl().get());
    }

    @Test
    public void testLinkableItemFieldsMissingUrl() {
        final String name = "itemName";
        final String value = "itemValue";
        final LinkableItem item = new LinkableItem(name, value);

        assertEquals(name, item.getName());
        assertEquals(value, item.getValue());
        assertFalse(item.getUrl().isPresent());
    }
}
