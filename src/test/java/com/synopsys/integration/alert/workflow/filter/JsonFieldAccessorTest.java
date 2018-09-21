package com.synopsys.integration.alert.workflow.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.ObjectHierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;

public class JsonFieldAccessorTest {

    @Test
    public void getStringListTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final StringHierarchicalField expectedField = new StringHierarchicalField(Collections.emptyList(), "innerString", null, null);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstStringTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final StringHierarchicalField expectedField = new StringHierarchicalField(Collections.emptyList(), "innerString", null, null);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getObjectListTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getTypedObjectListTest() throws AlertException {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField, MyObject.class));
    }

    @Test
    public void getTypedObjectThrowsExceptionTestTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        try {
            accessor.get(expectedField, NotMyObject.class);
            Assert.fail("Expected AlertException to be thrown");
        } catch (final AlertException e) {
            // Pass
        }
    }

    @Test
    public void getFirstTypedObjectTest() throws AlertException {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField, MyObject.class));
    }

    @Test
    public void getStringWhenListIsEmptyTest() {
        final StringHierarchicalField expectedField = new StringHierarchicalField(Collections.emptyList(), "innerString", null, null);
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getObjectWhenListIsEmptyTest() {
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectWhenListIsEmptyTest() {
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.empty(), accessor.getFirst(expectedField));
    }

    @Test
    public void getTypedObjectWhenMapIsEmptyTest() throws AlertException {
        final ObjectHierarchicalField expectedField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<MyObject>() {}.getType());
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Collections.emptyList(), accessor.get(expectedField, MyObject.class));
    }

    private class MyObject {
        public String myField;

        public MyObject(final String myField) {
            this.myField = myField;
        }

        @Override
        public boolean equals(final Object obj) {
            return myField != null && MyObject.class.isAssignableFrom(obj.getClass()) && myField.equals(((MyObject) obj).myField);
        }
    }

    private class NotMyObject {
        // For exception test
    }
}
