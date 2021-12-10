package com.synopsys.integration.alert.web.api.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteSaveActions;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class GlobalFieldModelToConcreteConversionServiceTest {
    private static final String TEST_DESCRIPTOR_KEY = "test_descriptor_key";

    private DescriptorKey testDescriptorKey = new DescriptorKey(TEST_DESCRIPTOR_KEY, TEST_DESCRIPTOR_KEY) {
        private static final long serialVersionUID = 1035004523393110640L;
    };

    @Test
    public void createdDescriptorFoundTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.GLOBAL.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createConcreteModel(fieldModel);
        assertTrue(saveActions.wasCreatedCalled());
    }

    @Test
    public void updatedDescriptorFoundTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.GLOBAL.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateConcreteModel(fieldModel);
        assertTrue(saveActions.wasUpdateCalled());
    }

    @Test
    public void createdDescriptorMissingTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.GLOBAL.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createConcreteModel(fieldModel);
        assertFalse(saveActions.wasCreatedCalled());
    }

    @Test
    public void updatedDescriptorMissingTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.GLOBAL.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateConcreteModel(fieldModel);
        assertFalse(saveActions.wasUpdateCalled());
    }

    @Test
    public void createdDescriptorFoundNotGlobalTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.DISTRIBUTION.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.createConcreteModel(fieldModel);
        assertFalse(saveActions.wasCreatedCalled());
    }

    @Test
    public void updateDescriptorFoundNotGlobalTest() {
        TestSaveActions saveActions = new TestSaveActions(testDescriptorKey);
        DescriptorMap descriptorMap = new DescriptorMap(List.of(testDescriptorKey), List.of());
        List<TestSaveActions> fieldModelSaveActions = List.of(saveActions);
        FieldModel fieldModel = new FieldModel(TEST_DESCRIPTOR_KEY, ConfigContextEnum.DISTRIBUTION.name(), Map.of());

        GlobalFieldModelToConcreteConversionService conversionService = new GlobalFieldModelToConcreteConversionService(fieldModelSaveActions, descriptorMap);
        conversionService.updateConcreteModel(fieldModel);
        assertFalse(saveActions.wasUpdateCalled());
    }

    private static class TestSaveActions implements GlobalFieldModelToConcreteSaveActions {
        private boolean createdCalled = false;
        private boolean updateCalled = false;

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

        @Override
        public DescriptorKey getDescriptorKey() {
            return descriptorKey;
        }

        @Override
        public void updateConcreteModel(FieldModel fieldModel) {
            this.updateCalled = true;
        }

        @Override
        public void createConcreteModel(FieldModel fieldModel) {
            this.createdCalled = true;
        }
    }
}
