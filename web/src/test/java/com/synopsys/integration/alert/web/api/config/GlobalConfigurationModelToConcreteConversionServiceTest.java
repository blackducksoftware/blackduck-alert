package com.synopsys.integration.alert.web.api.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class GlobalConfigurationModelToConcreteConversionServiceTest {
    private static final String TEST_DESCRIPTOR_KEY = "test_descriptor_key";

    private DescriptorKey testDescriptorKey = new DescriptorKey(TEST_DESCRIPTOR_KEY, TEST_DESCRIPTOR_KEY) {
        private static final long serialVersionUID = 1035004523393110640L;
    };

    @Test
    public void createdDescriptorFoundTest() {
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
    public void updatedDescriptorFoundTest() {
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
    public void deleteDescriptorFoundTest() {
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
    public void createdDescriptorMissingTest() {
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
    public void updatedDescriptorMissingTest() {
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
    public void deletedDescriptorMissingTest() {
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
    public void createdDescriptorFoundNotGlobalTest() {
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
    public void updateDescriptorFoundNotGlobalTest() {
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
    public void deletedDescriptorFoundNotGlobalTest() {
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

        private DescriptorKey descriptorKey;

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
