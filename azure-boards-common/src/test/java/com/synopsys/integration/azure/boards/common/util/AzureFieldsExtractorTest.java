package com.synopsys.integration.azure.boards.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class AzureFieldsExtractorTest {
    @Test
    public void extractFieldValidTest() {
        Gson gson = new GsonBuilder().create();
        AzureFieldsExtractor azureFieldsExtractor = new AzureFieldsExtractor(gson);

        FieldsTestClass originalCopy = createTestClass("a value", 42, "a different value", 8675309);
        String jsonString = gson.toJson(originalCopy);
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

        Optional<String> field1Value = azureFieldsExtractor.extractField(jsonObject, FieldsTestClass.FIELD_1);
        assertTrue(field1Value.isPresent(), "Field 1 was not present");
        assertEquals(originalCopy.getField1(), field1Value.get());

        Optional<Integer> field2Value = azureFieldsExtractor.extractField(jsonObject, FieldsTestClass.FIELD_2);
        assertTrue(field2Value.isPresent(), "Field 2 was not present");
        assertEquals(originalCopy.getField2(), field2Value.get());

        Optional<FieldsTestInnerClass> field3Value = azureFieldsExtractor.extractField(jsonObject, FieldsTestClass.FIELD_3);
        assertTrue(field3Value.isPresent(), "Field 3 was not present");

        FieldsTestInnerClass field3Object = field3Value.get();
        assertEquals(originalCopy.getField3().getInnerField1(), field3Object.getInnerField1());
        assertEquals(originalCopy.getField3().getInnerField2(), field3Object.getInnerField2());
    }

    private FieldsTestClass createTestClass(String field1, Integer field2, String innerField1, Integer innerField2) {
        FieldsTestInnerClass innerFields = new FieldsTestInnerClass(innerField1, innerField2);
        return new FieldsTestClass(field1, field2, innerFields);
    }

    private static class FieldsTestClass {
        public static final AzureFieldDefinition<String> FIELD_1 = AzureFieldDefinition.stringField("field1");
        public static final AzureFieldDefinition<Integer> FIELD_2 = AzureFieldDefinition.integerField("field2");
        public static final AzureFieldDefinition<FieldsTestInnerClass> FIELD_3 = new AzureFieldDefinition<>("field3", FieldsTestInnerClass.class);

        private final String field1;
        private final Integer field2;
        private final FieldsTestInnerClass field3;

        public FieldsTestClass(String field1, Integer field2, FieldsTestInnerClass field3) {
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

        public FieldsTestInnerClass getField3() {
            return field3;
        }

    }

    private static class FieldsTestInnerClass {
        private final String innerField1;
        private final Integer innerField2;

        public FieldsTestInnerClass(String innerField1, Integer innerField2) {
            this.innerField1 = innerField1;
            this.innerField2 = innerField2;
        }

        public String getInnerField1() {
            return innerField1;
        }

        public Integer getInnerField2() {
            return innerField2;
        }

    }

}
