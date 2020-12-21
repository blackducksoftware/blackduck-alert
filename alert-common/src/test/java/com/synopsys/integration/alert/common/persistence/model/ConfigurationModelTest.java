package com.synopsys.integration.alert.common.persistence.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;

public class ConfigurationModelTest {
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
    public void getDescriptorIdTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        assertEquals(descriptorId, configurationModel.getDescriptorId());
    }

    @Test
    public void getConfigurationIdTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        assertEquals(configurationId, configurationModel.getConfigurationId());
    }

    @Test
    public void getCreatedAtTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        assertEquals(createdAt, configurationModel.getCreatedAt());
    }

    @Test
    public void getLastUpdatedTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        assertEquals(lastUpdated, configurationModel.getLastUpdated());
    }

    @Test
    public void getDescriptorContextTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        assertEquals(configContextEnum, configurationModel.getDescriptorContext());
    }

    @Test
    public void getFieldTest() {
        ConfigurationModel configurationModel = createConfigurationModel();

        assertTrue(configurationModel.getField(fieldKey).isPresent());
        assertFalse(configurationModel.getField("badFieldKey").isPresent());
    }

    @Test
    public void getCopyOfFieldListTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        List<ConfigurationFieldModel> fieldList = configurationModel.getCopyOfFieldList();

        assertEquals(1, fieldList.size());
        assertEquals(configurationFieldModel, fieldList.get(0));
    }

    @Test
    public void getCopyOfKeyToFieldMapTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        Map<String, ConfigurationFieldModel> keyToFieldMap = configurationModel.getCopyOfKeyToFieldMap();

        assertFalse(keyToFieldMap.isEmpty());
        assertTrue(keyToFieldMap.containsValue(configurationFieldModel));
    }

    @Test
    public void createMutableCopyTest() {
        ConfigurationModel configurationModel = createConfigurationModel();
        ConfigurationModelMutable configurationModelMutable = configurationModel.createMutableCopy();

        assertEquals(configurationModel, configurationModelMutable);
        assertNotSame(configurationModel, configurationModelMutable);
    }

    private ConfigurationModel createConfigurationModel() {
        Map<String, ConfigurationFieldModel> configuredFields = Map.of(fieldKey, configurationFieldModel);
        return new ConfigurationModel(descriptorId, configurationId, createdAt, lastUpdated, configContextEnum, configuredFields);
    }

}

