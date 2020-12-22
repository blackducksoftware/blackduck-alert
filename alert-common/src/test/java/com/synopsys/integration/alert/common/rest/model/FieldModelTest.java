package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FieldModelTest {
    @Test
    public void fieldModelNoIdTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);

        assertEquals(descriptor, testFieldModel.getDescriptorName());
        assertEquals(context, testFieldModel.getContext());
        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertNull(testFieldModel.getId());
        assertNull(testFieldModel.getCreatedAt());
        assertNull(testFieldModel.getLastUpdated());
    }

    @Test
    public void fieldModelWithIdTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String configId = "Id1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(configId, descriptor, context, keyToValues);

        assertEquals(descriptor, testFieldModel.getDescriptorName());
        assertEquals(context, testFieldModel.getContext());
        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertEquals(configId, testFieldModel.getId());
        assertNull(testFieldModel.getCreatedAt());
        assertNull(testFieldModel.getLastUpdated());
    }

    @Test
    public void fieldModelWithTimeStampsTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String createdAt = "2020-01-22-14-11-20-124";
        String lastUpdated = "2020-01-1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, createdAt, lastUpdated, keyToValues);

        assertEquals(descriptor, testFieldModel.getDescriptorName());
        assertEquals(context, testFieldModel.getContext());
        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertNull(testFieldModel.getId());
        assertEquals(createdAt, testFieldModel.getCreatedAt());
        assertEquals(lastUpdated, testFieldModel.getLastUpdated());
    }

    @Test
    public void fieldModelTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String configId = "Id1";
        String createdAt = "2020-01-22-14-11-20-124";
        String lastUpdated = "2020-01-1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(configId, descriptor, context, createdAt, lastUpdated, keyToValues);

        assertEquals(descriptor, testFieldModel.getDescriptorName());
        assertEquals(context, testFieldModel.getContext());
        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertEquals(configId, testFieldModel.getId());
        assertEquals(createdAt, testFieldModel.getCreatedAt());
        assertEquals(lastUpdated, testFieldModel.getLastUpdated());
    }

    @Test
    public void setKeyToValuesTest() {
        String descriptor = "description1";
        String context = "context1";
        String key1 = "original key";
        String key2 = "key to be added into keyToValues";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key1, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);

        assertTrue(testFieldModel.getKeyToValues().containsKey(key1));
        assertFalse(testFieldModel.getKeyToValues().containsKey(key2));

        keyToValues.put(key2, testFieldValueModel);
        testFieldModel.setKeyToValues(keyToValues);

        assertTrue(testFieldModel.getKeyToValues().containsKey(key1));
        assertTrue(testFieldModel.getKeyToValues().containsKey(key2));
    }

    @Test
    public void getFieldValueModelTest() {
        String descriptor = "description1";
        String context = "context1";
        String keyValid = "Key1";
        String keyBad = "This key is not in the Field Model";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(keyValid, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);
        Optional<FieldValueModel> verifyFieldValueModel = testFieldModel.getFieldValueModel(keyValid);

        assertTrue(verifyFieldValueModel.isPresent());
        assertEquals(testFieldValueModel, verifyFieldValueModel.get());

        verifyFieldValueModel = testFieldModel.getFieldValueModel(keyBad);

        assertFalse(verifyFieldValueModel.isPresent());
    }

    @Test
    public void getFieldValueTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String badKey = "This key doesn't exist";
        String testFieldValue = "FieldValue1";

        List<String> values = new ArrayList();
        values.add(testFieldValue);
        FieldValueModel newFieldValueModel = new FieldValueModel(values, Boolean.TRUE);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, newFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);
        Optional<String> value = testFieldModel.getFieldValue(key);

        assertEquals(testFieldValue, value.get());
        assertTrue(value.isPresent());

        value = testFieldModel.getFieldValue(badKey);

        assertFalse(value.isPresent());
    }

    @Test
    public void getEmptyFieldValueTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String keyWithoutValue = "Key without fieldValue";

        List<String> emptyValues = new ArrayList();
        FieldValueModel emptyFieldValueModel = new FieldValueModel(emptyValues, Boolean.TRUE);

        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(keyWithoutValue, emptyFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);
        testFieldModel.setKeyToValues(keyToValues);
        Optional<String> value = testFieldModel.getFieldValue(key);

        assertFalse(value.isPresent());
    }

    @Test
    public void putFieldTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String newKey = "Key2";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);

        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertFalse(testFieldModel.getKeyToValues().containsKey(newKey));

        testFieldModel.putField(newKey, testFieldValueModel);

        assertTrue(testFieldModel.getKeyToValues().containsKey(key));
        assertTrue(testFieldModel.getKeyToValues().containsKey(newKey), "The newKey was not successfully added into the testFieldModel.");
    }

    @Test
    public void removeFieldTest() {
        String descriptor = "description1";
        String context = "context1";
        String keyValid = "Key1";
        String keyToDelete = "Key2";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(keyValid, testFieldValueModel);
        keyToValues.put(keyToDelete, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(descriptor, context, keyToValues);
        Map<String, FieldValueModel> testKeyToValues = testFieldModel.getKeyToValues();

        assertTrue(testKeyToValues.containsKey(keyValid));
        assertTrue(testKeyToValues.containsKey(keyToDelete));

        testFieldModel.removeField(keyToDelete);
        testKeyToValues = testFieldModel.getKeyToValues();

        assertTrue(testKeyToValues.containsKey(keyValid));
        assertFalse(testKeyToValues.containsKey(keyToDelete), "The keyToDelete was not successfully removed.");
    }

    @Test
    public void fillTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String configId = "Id1";
        String createdAt = "2020-01-22-14-11-20-124";
        String lastUpdated = "2020-01-1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModel = new FieldModel(configId, descriptor, context, createdAt, lastUpdated, keyToValues);
        FieldModel newTestFieldModel = testFieldModel.fill(testFieldModel);

        assertEquals(testFieldModel, newTestFieldModel);
        assertNotSame(testFieldModel, newTestFieldModel);
        assertEquals(descriptor, newTestFieldModel.getDescriptorName());
        assertEquals(context, newTestFieldModel.getContext());
        assertTrue(newTestFieldModel.getKeyToValues().containsKey(key));
        assertEquals(configId, newTestFieldModel.getId());
        assertEquals(createdAt, newTestFieldModel.getCreatedAt());
        assertEquals(lastUpdated, newTestFieldModel.getLastUpdated());
    }

    @Test
    public void fillExistingFieldModelTest() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String key2 = "Key2";
        String configId = "Id1";
        String createdAt = "2020-01-22-14-11-20-124";
        String lastUpdated = "2020-01-1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModelWithValues = new FieldModel(configId, descriptor, context, createdAt, lastUpdated, keyToValues);

        Map<String, FieldValueModel> newKeyToValues = new HashMap<>();
        newKeyToValues.put(key2, testFieldValueModel);
        FieldModel testFieldModelMissingValues = new FieldModel(null, null, null, null, null, newKeyToValues);

        FieldModel testCombinedFieldModel = testFieldModelMissingValues.fill(testFieldModelWithValues);

        assertEquals(descriptor, testCombinedFieldModel.getDescriptorName());
        assertEquals(context, testCombinedFieldModel.getContext());
        assertEquals(configId, testCombinedFieldModel.getId());
        assertEquals(createdAt, testCombinedFieldModel.getCreatedAt());
        assertEquals(lastUpdated, testCombinedFieldModel.getLastUpdated());

        assertFalse(testFieldModelWithValues.getKeyToValues().containsKey(key2));
        assertFalse(testFieldModelMissingValues.getKeyToValues().containsKey(key));
        assertTrue(testCombinedFieldModel.getKeyToValues().containsKey(key), "The testCombinedFieldModel is missing the key: " + key);
        assertTrue(testCombinedFieldModel.getKeyToValues().containsKey(key2), "The testCombinedFieldModel is missing the key: " + key2);
    }

    @Test
    public void fillFieldValueModelEmpty() {
        String descriptor = "description1";
        String context = "context1";
        String key = "Key1";
        String configId = "Id1";
        String createdAt = "2020-01-22-14-11-20-124";
        String lastUpdated = "2020-01-1";

        FieldValueModel testFieldValueModel = Mockito.mock(FieldValueModel.class);
        FieldValueModel emptyFieldValueModel = Mockito.mock(FieldValueModel.class);
        Mockito.when(emptyFieldValueModel.getValue()).thenReturn(Optional.empty());
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(key, testFieldValueModel);
        FieldModel testFieldModelWithValues = new FieldModel(configId, descriptor, context, createdAt, lastUpdated, keyToValues);

        Map<String, FieldValueModel> newKeyToValues = new HashMap<>();
        newKeyToValues.put(key, emptyFieldValueModel);
        FieldModel testFieldModelMissingValues = new FieldModel(configId, descriptor, context, createdAt, lastUpdated, newKeyToValues);

        FieldModel testCombinedFieldModel = testFieldModelMissingValues.fill(testFieldModelWithValues);

        assertTrue(testCombinedFieldModel.getKeyToValues().containsKey(key));
        assertEquals(testFieldValueModel, testCombinedFieldModel.getFieldValueModel(key).get());
    }

}
