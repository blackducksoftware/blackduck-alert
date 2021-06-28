package com.synopsys.integration.alert.api.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AlertSerializableModelTest {
    private final Gson gson = new GsonBuilder().create();

    @Test
    public void serializeTest() {
        TestModel testModel = new TestModel("test value", 33, "transient value");
        String jsonModel = gson.toJson(testModel);
        TestModel deserializedTestModel = gson.fromJson(jsonModel, TestModel.class);
        assertEquals(testModel.getField1(), deserializedTestModel.getField1());
        assertEquals(testModel.getField2(), deserializedTestModel.getField2());
        assertNull(deserializedTestModel.getField3(), "Expected transient field not to be included in deserialization");
    }

    private static class TestModel extends AlertSerializableModel {
        private final String field1;
        private final Integer field2;
        private final transient String field3;

        public TestModel(String field1, Integer field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public String getField1() {
            return field1;
        }

        public Integer getField2() {
            return field2;
        }

        public String getField3() {
            return field3;
        }

    }

}
