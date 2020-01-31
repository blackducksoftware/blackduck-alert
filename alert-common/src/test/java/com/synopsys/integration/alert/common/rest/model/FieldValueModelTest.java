package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FieldValueModelTest {
    String testValue = "valueToTest";

    private FieldValueModel createEmptyFieldValueModel() {
        List<String> values = new ArrayList();
        return new FieldValueModel(values, Boolean.TRUE);
    }

    @Test
    public void getValuesTest() {
        String duplicateTestValue = testValue;
        List<String> values = new ArrayList();
        values.add(testValue);
        values.add(duplicateTestValue);
        FieldValueModel testFieldValueModel = new FieldValueModel(values, Boolean.TRUE);

        assertTrue(testFieldValueModel.getValues().containsAll(values));
        // getValues() will create a Set, this assert verifies duplicate values removed
        assertTrue(testFieldValueModel.getValues().size() == 1);
    }

    @Test
    public void getNullValuesTest() {
        FieldValueModel testFieldValueModel = createEmptyFieldValueModel();

        assertTrue(testFieldValueModel.getValues().isEmpty());
    }

    @Test
    public void getValueTest() {
        List<String> values = new ArrayList();
        FieldValueModel emptyFieldValueModel = createEmptyFieldValueModel();
        values.add(testValue);
        FieldValueModel testFieldValueModel = new FieldValueModel(values, Boolean.TRUE);

        assertFalse(emptyFieldValueModel.getValue().isPresent());
        assertTrue(testFieldValueModel.getValue().isPresent());
        assertEquals(testValue, testFieldValueModel.getValue().get());
    }

    @Test
    public void getValueFindFirstTest() {
        String extraTestValue = "new-valueToTest";
        List<String> values = new ArrayList();
        values.add(testValue);
        values.add(extraTestValue);
        FieldValueModel testFieldValueModel = new FieldValueModel(values, Boolean.TRUE);

        // getValue will return the last value that was added into the collection
        assertEquals(extraTestValue, testFieldValueModel.getValue().get());
        assertTrue(testFieldValueModel.getValues().size() == 2);
    }

    @Test
    public void setValueTest() {
        String overwrittenTestValue = "new-value-overwriting-testValue";
        FieldValueModel testFieldValueModel = createEmptyFieldValueModel();
        testFieldValueModel.setValue(testValue);
        testFieldValueModel.setValue(overwrittenTestValue);

        assertTrue(testFieldValueModel.getValues().contains(overwrittenTestValue));
        assertTrue(testFieldValueModel.getValues().size() == 1);
    }

    @Test
    public void setIsSetTest() {
        FieldValueModel testFieldValueModel = createEmptyFieldValueModel();
        testFieldValueModel.setIsSet(Boolean.FALSE);

        assertFalse(testFieldValueModel.isSet());
    }

    @Test
    public void hasValuesTest() {
        FieldValueModel testFieldValueModel = createEmptyFieldValueModel();
        FieldValueModel testFieldValueModelNoValues = createEmptyFieldValueModel();
        List<String> values = new ArrayList();
        values.add(testValue);
        testFieldValueModel.setValues(values);

        assertTrue(testFieldValueModel.hasValues());
        assertFalse(testFieldValueModelNoValues.hasValues());
    }

    @Test
    public void containsNoDataTest() {
        FieldValueModel testFieldValueModelNoValueIsNotSet = createEmptyFieldValueModel();
        testFieldValueModelNoValueIsNotSet.setIsSet((Boolean.FALSE));
        FieldValueModel testFieldValueModelNoValue = createEmptyFieldValueModel();
        FieldValueModel testFieldValueModelIsNotSet = createEmptyFieldValueModel();
        testFieldValueModelIsNotSet.setValue(testValue);
        testFieldValueModelIsNotSet.setIsSet(Boolean.FALSE);
        FieldValueModel testFieldValueModel = createEmptyFieldValueModel();
        testFieldValueModel.setValue(testValue);

        assertTrue(testFieldValueModelNoValueIsNotSet.containsNoData());
        assertFalse(testFieldValueModelNoValue.containsNoData());
        assertFalse(testFieldValueModelIsNotSet.containsNoData());
        assertFalse(testFieldValueModel.containsNoData());
    }
}