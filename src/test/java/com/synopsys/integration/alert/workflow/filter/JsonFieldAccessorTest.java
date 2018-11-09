package com.synopsys.integration.alert.workflow.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;

public class JsonFieldAccessorTest {

    @Test
    public void getStringListTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final HierarchicalField<String> expectedField = HierarchicalField.createStringField(Arrays.asList("innerString"), null, null);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstStringTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final HierarchicalField<String> expectedField = HierarchicalField.createStringField(Arrays.asList("innerString"), null, null);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getObjectListTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final HierarchicalField<MyObject> expectedField = HierarchicalField.createObjectField(Arrays.asList("innerObject"), null, null, MyObject.class);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final HierarchicalField<MyObject> expectedField = HierarchicalField.createObjectField(Arrays.asList("innerObject"), null, null, MyObject.class);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getStringWhenListIsEmptyTest() {
        final HierarchicalField<String> expectedField = HierarchicalField.createStringField(Arrays.asList("innerString"), null, null);
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getObjectWhenListIsEmptyTest() {
        final HierarchicalField<MyObject> expectedField = HierarchicalField.createObjectField(Arrays.asList("innerObject"), null, null, MyObject.class);
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectWhenListIsEmptyTest() {
        final HierarchicalField<MyObject> expectedField = HierarchicalField.createObjectField(Arrays.asList("innerObject"), null, null, MyObject.class);
        final List<Object> expectedValues = Collections.emptyList();
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.empty(), accessor.getFirst(expectedField));
    }

    @Test
    public void getTypedObjectWhenMapIsEmptyTest() {
        final HierarchicalField<MyObject> expectedField = HierarchicalField.createObjectField(Arrays.asList("innerObject"), null, null, MyObject.class);
        final Map<HierarchicalField, List<Object>> map = new HashMap<>();

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Collections.emptyList(), accessor.get(expectedField));
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
}
