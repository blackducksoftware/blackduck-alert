package com.synopsys.integration.alert.common.descriptor.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public class DefaultDescriptorGlobalConfigUtilityTest {

    private DescriptorKey createDescriptorKey() {
        return new DescriptorKey() {
            private static final long serialVersionUID = 6317053803499005970L;

            @Override
            public String getUniversalKey() {
                return "universal_key";
            }

            @Override
            public String getDisplayName() {
                return "Universal Key";
            }
        };
    }

    @Test
    public void testGetKey() {
        DescriptorKey descriptorKey = createDescriptorKey();
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, null, null, null);
        assertEquals(descriptorKey, configUtility.getKey());
    }

    @Test
    public void testConfigurationExists() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, null, null);
        assertFalse(configUtility.doesConfigurationExist());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));

        assertTrue(configUtility.doesConfigurationExist());
    }

    @Test
    public void testConfigurationExistsWithException() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenThrow(new AlertDatabaseConstraintException("Test exception"));
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, null, null);
        assertFalse(configUtility.doesConfigurationExist());
    }

    @Test
    public void testGetConfiguration() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, null, null);
        assertFalse(configUtility.getConfiguration().isPresent());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));

        assertTrue(configUtility.getConfiguration().isPresent());
    }

    @Test
    public void testGetFieldModelEmptyConfiguration() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, null, converter);
        assertFalse(configUtility.getFieldModel().isPresent());
    }

    @Test
    public void testGetFieldModel() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, apiAction, converter);
        FieldModel actualFieldModel = configUtility.getFieldModel().orElse(null);
        assertEquals(fieldModel, actualFieldModel);
    }

    @Test
    public void testGetFieldModelActionNull() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(Mockito.eq(fieldModel))).thenReturn(null);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, apiAction, converter);
        Optional<FieldModel> actualFieldModel = configUtility.getFieldModel();
        assertFalse(actualFieldModel.isPresent());
    }

    @Test
    public void testSave() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(Mockito.eq(fieldModel))).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeSaveAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(apiAction.afterSaveAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.save(fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationAccessor).createConfiguration(Mockito.eq(descriptorKey), Mockito.eq(ConfigContextEnum.GLOBAL), Mockito.anyCollection());
    }

    @Test
    public void testUpdate() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(Mockito.eq(fieldModel))).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeUpdateAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(apiAction.afterUpdateAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.update(1L, fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }
}
