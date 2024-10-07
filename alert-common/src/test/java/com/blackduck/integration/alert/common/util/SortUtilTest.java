package com.blackduck.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class SortUtilTest {

    @Test
    void testNullArguments() {
        Sort sort = SortUtil.createSortByFieldName(null, null);
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void testNullDirection() {
        Sort sort = SortUtil.createSortByFieldName("fieldName", null);
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void testEmptyDirection() {
        Sort sort = SortUtil.createSortByFieldName("fieldName", "    \t\n\r      ");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void testNullFieldName() {
        Sort sort = SortUtil.createSortByFieldName(null, "asc");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void testEmptyFieldName() {
        Sort sort = SortUtil.createSortByFieldName("    \t\n\r      ", "asc");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    void testAscendingSort() {
        String fieldName = "fieldName";
        Sort sort = SortUtil.createSortByFieldName(fieldName, "asc");
        Sort.Order sortOrder = sort.getOrderFor(fieldName);
        assertNotNull(sortOrder);
        assertTrue(sortOrder.isAscending());
    }

    @Test
    void testDescendingSort() {
        String fieldName = "fieldName";
        Sort sort = SortUtil.createSortByFieldName(fieldName, "desc");
        Sort.Order sortOrder = sort.getOrderFor(fieldName);
        assertNotNull(sortOrder);
        assertTrue(sortOrder.isDescending());
    }

    @Test
    void testInvalidDirection() {
        String fieldName = "fieldName";
        Sort sort = SortUtil.createSortByFieldName(fieldName, "wrongway");
        assertEquals(Sort.unsorted(), sort);
    }

}
