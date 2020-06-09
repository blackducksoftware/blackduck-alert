package com.synopsys.integration.alert.common.persistence.model.mutable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class ConfigurationModelMutableTest {
    private final Long descriptorId = 1L;
    private final Long configurationId = 2L;
    private final String createdAt = "createdAt-test";
    private final String lastUpdated = "lastUpdated-test";
    private final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
    private final String fieldKey = "fieldKey";
    private final String fieldValue = "fieldValue";

    private ConfigurationFieldModel configurationFieldModel;

    @BeforeEach
    public void init() {
        configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel.setFieldValue(fieldValue);
    }

    @Test
    public void putTest() {
        final String fieldValue2 = "fieldValue-2";

        ConfigurationFieldModel configurationFieldModel2 = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel2.setFieldValue(fieldValue2);

        ConfigurationModelMutable configurationModelMutable = createConfigurationModelMutable();
        configurationModelMutable.put(configurationFieldModel);
        configurationModelMutable.put(configurationFieldModel2);

        Optional<ConfigurationFieldModel> testConfigurationFieldModelOptional = configurationModelMutable.getField(fieldKey);

        assertTrue(testConfigurationFieldModelOptional.isPresent());
        ArrayList<String> fieldValues = new ArrayList<>(testConfigurationFieldModelOptional.get().getFieldValues());

        assertEquals(2, fieldValues.size());
        assertTrue(fieldValues.contains(fieldValue));
        assertTrue(fieldValues.contains(fieldValue2));
    }

    @Test
    public void getFieldTest() {
        ConfigurationModelMutable configurationModelMutable = new ConfigurationModelMutable(descriptorId, configurationId, createdAt, lastUpdated, configContextEnum.name());
        configurationModelMutable.put(configurationFieldModel);

        Optional<ConfigurationFieldModel> testConfigurationFieldModel = configurationModelMutable.getField(fieldKey);
        Optional<ConfigurationFieldModel> testConfigurationFieldModelEmpty = configurationModelMutable.getField("badKey");

        assertTrue(testConfigurationFieldModel.isPresent());
        assertFalse(testConfigurationFieldModelEmpty.isPresent());
        assertEquals(configurationFieldModel, testConfigurationFieldModel.get());
    }

    @Test
    public void getCopyOfFieldListTest() {
        ConfigurationModelMutable configurationModelMutable = createConfigurationModelMutable();
        configurationModelMutable.put(configurationFieldModel);

        List<ConfigurationFieldModel> testConfigurationFieldModelList = configurationModelMutable.getCopyOfFieldList();

        assertEquals(1, testConfigurationFieldModelList.size());
        assertEquals(configurationFieldModel, testConfigurationFieldModelList.get(0));
    }

    @Test
    public void getCopyOfKeyToFieldMapTest() {
        ConfigurationModelMutable configurationModelMutable = createConfigurationModelMutable();
        configurationModelMutable.put(configurationFieldModel);

        Map<String, ConfigurationFieldModel> configurationFieldModelMap = configurationModelMutable.getCopyOfKeyToFieldMap();

        assertEquals(1, configurationFieldModelMap.size());
        assertEquals(configurationFieldModel, configurationFieldModelMap.get(fieldKey));
    }

    private ConfigurationModelMutable createConfigurationModelMutable() {
        return new ConfigurationModelMutable(descriptorId, configurationId, createdAt, lastUpdated, configContextEnum);
    }

}
