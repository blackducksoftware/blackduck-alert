package com.blackduck.integration.alert.web.api.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.blackduck.integration.alert.common.descriptor.DescriptorMap;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.util.DateUtils;

class GlobalConfigurationModelToConcreteConversionServiceTest {
    private static final String TEST_DESCRIPTOR_KEY = "test_descriptor_key";

    private DescriptorKey testDescriptorKey = new DescriptorKey(TEST_DESCRIPTOR_KEY, TEST_DESCRIPTOR_KEY) {
        private static final long serialVersionUID = 1035004523393110640L;
    };

    @Test
    void createdDescriptorFoundTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertTrue(saveActions.wasCreatedCalled());
    }

    @Test
    void updatedDescriptorFoundTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertTrue(saveActions.wasUpdateCalled());
    }

    @Test
    void deleteDescriptorFoundTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.deleteDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertTrue(saveActions.wasDeleteCalled());
    }

    @Test
    void createdDescriptorMissingTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasCreatedCalled());
    }

    @Test
    void updatedDescriptorMissingTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasUpdateCalled());
    }

    @Test
    void deletedDescriptorMissingTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.GLOBAL, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.deleteDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasDeleteCalled());
    }

    @Test
    void createdDescriptorFoundNotGlobalTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.DISTRIBUTION, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasCreatedCalled());
    }

    @Test
    void updateDescriptorFoundNotGlobalTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.DISTRIBUTION, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasUpdateCalled());
    }

    @Test
    void deletedDescriptorFoundNotGlobalTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<GlobalConfigurationModelToConcreteSaveActions> fieldModelSaveActions = List.of(saveActions);
        String timestamp = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, timestamp, timestamp, ConfigContextEnum.DISTRIBUTION, Map.of());

        GlobalConfigurationModelToConcreteConversionService conversionService = new GlobalConfigurationModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.deleteDefaultConcreteModel(TEST_DESCRIPTOR_KEY, configurationModel);
        assertFalse(saveActions.wasDeleteCalled());
    }

    private static class TestSaveActions implements GlobalConfigurationModelToConcreteSaveActions {
        private boolean createdCalled = false;
        private boolean updateCalled = false;
        private boolean deleteCalled = false;

        private final DescriptorKey descriptorKey;

        public TestSaveActions(DescriptorKey descriptorKey) {
            this.descriptorKey = descriptorKey;
        }

        public boolean wasCreatedCalled() {
            return createdCalled;
        }

        public boolean wasUpdateCalled() {
            return updateCalled;
        }

        public boolean wasDeleteCalled() {return deleteCalled;}

        @Override
        public DescriptorKey getDescriptorKey() {
            return descriptorKey;
        }

        @Override
        public void updateConcreteModel(ConfigurationModel configurationModel) {
            this.updateCalled = true;
        }

        @Override
        public void createConcreteModel(ConfigurationModel configurationModel) {
            this.createdCalled = true;
        }

        @Override
        public void deleteConcreteModel(ConfigurationModel configurationModel) {
            this.deleteCalled = true;
        }
    }
}
