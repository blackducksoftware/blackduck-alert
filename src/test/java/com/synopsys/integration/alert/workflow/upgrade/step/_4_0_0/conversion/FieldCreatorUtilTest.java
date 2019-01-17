package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;

public class FieldCreatorUtilTest {

    @Test
    public void createFieldModelsTest() {
        final FieldCreatorUtil fieldCreatorUtil = new FieldCreatorUtil();

        final ConfigurationFieldModel fieldModel = fieldCreatorUtil.createFieldModel("key", "value");
        final ConfigurationFieldModel sensitiveFieldModel = fieldCreatorUtil.createSensitiveFieldModel("sensitiveKey", "sensitiveValue");

        assertEquals("key", fieldModel.getFieldKey());
        assertEquals("value", fieldModel.getFieldValue().orElse(""));
        assertFalse(fieldModel.isSensitive());
        assertTrue(sensitiveFieldModel.isSensitive());
    }

    @Test
    public void addFieldModelsTest() {
        final FieldCreatorUtil fieldCreatorUtil = new FieldCreatorUtil();

        final List<ConfigurationFieldModel> fieldModels = new LinkedList<>();

        fieldCreatorUtil.addFieldModel("key", "value", fieldModels);
        fieldCreatorUtil.addFieldModel("null", null, fieldModels);
        fieldCreatorUtil.addSecureFieldModel("secure", "secure", fieldModels);

        final Map<String, ConfigurationFieldModel> fieldModelMap = fieldModels.stream().collect(Collectors.toMap(configurationFieldModel -> configurationFieldModel.getFieldKey(), Function.identity()));

        assertEquals(2, fieldModels.size());
        assertEquals("value", fieldModelMap.get("key").getFieldValue().orElse(""));
        assertNull(fieldModelMap.get("null"));
        assertTrue(fieldModelMap.get("secure").isSensitive());
    }
}
