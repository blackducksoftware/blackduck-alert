package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CategoryKeyTest {

    @Test
    public void testFromName() {
        final String name = "categoryKeyName";
        final CategoryKey categoryKey = CategoryKey.fromName(name);

        assertEquals(name, categoryKey.getKey());
    }

    @Test
    public void testFromPair() {
        final String name = "categoryKeyName";
        final String value = "categoryKeyValue";
        final String formattedKey = String.format("%s_%s", name, value);
        final CategoryKey categoryKey = CategoryKey.fromPair(name, value);

        assertEquals(formattedKey, categoryKey.getKey());
    }
}
