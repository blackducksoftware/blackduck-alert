package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class ConfigurationTest {
    @Test
    public void getFieldAccessorTest() {
        String fieldKey = "Key1";

        ConfigurationFieldModel testConfigurationFieldModel = Mockito.mock(ConfigurationFieldModel.class);
        Map<String, ConfigurationFieldModel> keyToFieldMapTest = new HashMap<>();
        keyToFieldMapTest.put(fieldKey, testConfigurationFieldModel);
        Configuration testConfig = new Configuration(keyToFieldMapTest);
        FieldUtility testFieldUtility = testConfig.getFieldUtility();
        Optional<ConfigurationFieldModel> newConfigurationFieldModel = testFieldUtility.getField(fieldKey);

        assertTrue(newConfigurationFieldModel.isPresent());
        assertEquals(testConfigurationFieldModel, newConfigurationFieldModel.get());
    }
}
