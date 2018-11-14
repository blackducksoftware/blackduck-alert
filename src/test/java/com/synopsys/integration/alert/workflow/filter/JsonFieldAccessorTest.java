package com.synopsys.integration.alert.workflow.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;

public class JsonFieldAccessorTest {

    @Test
    public void getStringListTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final JsonField<String> expectedField = JsonField.createStringField(JsonPath.compile("$.innerString"), "innerString", null, null);
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstStringTest() {
        final List<Object> expectedValues = Arrays.asList("value", "other value", "multi \n line \n value");
        final JsonField<String> expectedField = JsonField.createStringField(JsonPath.compile("$.innerString"), "innerString", null, null);
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getObjectListTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final JsonField<MyObject> expectedField = JsonField.createObjectField(JsonPath.compile("$.innerString"), "innerString", null, null, new TypeRef<MyObject>() {});
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectTest() {
        final List<Object> expectedValues = Arrays.asList(new MyObject("first value"), new MyObject("second value"));
        final JsonField<MyObject> expectedField = JsonField.createObjectField(JsonPath.compile("$.innerObject"), "innerObject", null, null, new TypeRef<MyObject>() {});
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.of(expectedValues.get(0)), accessor.getFirst(expectedField));
    }

    @Test
    public void getStringWhenListIsEmptyTest() {
        final JsonField<String> expectedField = JsonField.createStringField(JsonPath.compile("$.innerString"), "innerString", null, null);
        final List<Object> expectedValues = Collections.emptyList();
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getObjectWhenListIsEmptyTest() {
        final JsonField<MyObject> expectedField = JsonField.createObjectField(JsonPath.compile("$.innerString"), "innerString", null, null, new TypeRef<MyObject>() {});
        final List<Object> expectedValues = Collections.emptyList();
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(expectedValues, accessor.get(expectedField));
    }

    @Test
    public void getFirstObjectWhenListIsEmptyTest() {
        final JsonField<MyObject> expectedField = JsonField.createObjectField(JsonPath.compile("$.innerObject"), "innerObject", null, null, new TypeRef<MyObject>() {});
        final List<Object> expectedValues = Collections.emptyList();
        final Map<JsonField, List<Object>> map = new HashMap<>();
        map.put(expectedField, expectedValues);

        final JsonFieldAccessor accessor = new JsonFieldAccessor(map);
        Assert.assertEquals(Optional.empty(), accessor.getFirst(expectedField));
    }

    @Test
    public void getTypedObjectWhenMapIsEmptyTest() {
        final JsonField<MyObject> expectedField = JsonField.createObjectField(JsonPath.compile("$.innerObject"), "innerObject", null, null, new TypeRef<MyObject>() {});
        final Map<JsonField, List<Object>> map = new HashMap<>();

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
