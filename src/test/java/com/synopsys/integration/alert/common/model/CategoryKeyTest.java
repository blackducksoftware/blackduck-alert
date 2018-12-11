package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CategoryKeyTest {

    @Test
    public void testFromType() {
        final String type = "categoryType";
        final CategoryKey categoryKey = CategoryKey.from(type);

        assertEquals(type, categoryKey.getType());
    }

    @Test
    public void testFromTypeAndComponents() {
        final String type = "categoryType";
        final String part1 = "categoryKeyPartPreSorted1";
        final String part2 = "categoryKeyPartPreSorted2";
        final String expectedKey = String.join("_", part1, part2);
        final CategoryKey categoryKey = CategoryKey.from(type, part1, part2);

        assertEquals(type, categoryKey.getType());
        assertEquals(expectedKey, categoryKey.getKey());
    }

    @Test
    public void testFromSortingKeyParts() {
        final String type = "categoryType";
        final String part1 = "categoryKeyPart-1";
        final String part2 = "categoryKeyPart-2";
        final String expectedKey = String.join("_", part1, part2);
        final CategoryKey categoryKey = CategoryKey.from(type, part2, part1);

        assertEquals(type, categoryKey.getType());
        assertEquals(expectedKey, categoryKey.getKey());
    }

    @Test
    public void testFromTypeAndComponentsList() {
        final String type = "categoryType";
        final String part1 = "categoryKeyPartPreSorted1";
        final String part2 = "categoryKeyPartPreSorted2";
        final CategoryKey categoryKeyFromArray = CategoryKey.from(type, part2, part1);
        final List<String> componentList = Arrays.asList(part1, part2);
        final CategoryKey categoryKey = CategoryKey.from(type, componentList);

        assertEquals(type, categoryKey.getType());
        assertEquals(categoryKeyFromArray.getKey(), categoryKey.getKey());
    }
}
