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
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DefaultDescriptorGlobalConfigUtilityTest {

    private DescriptorKey createDescriptorKey() {
        return new DescriptorKey("universal_key", "Universal Key") {};
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
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, null);
        assertFalse(configUtility.doesConfigurationExist());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));

        assertTrue(configUtility.doesConfigurationExist());
    }
 
    @Test
    public void testGetConfiguration() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, null);
        assertFalse(configUtility.getConfiguration().isPresent());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));

        assertTrue(configUtility.getConfiguration().isPresent());
    }

    @Test
    public void testGetFieldModelEmptyConfiguration() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, converter);
        assertFalse(configUtility.getFieldModel().isPresent());
    }

    @Test
    public void testGetFieldModel() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel actualFieldModel = configUtility.getFieldModel().orElse(null);
        assertEquals(fieldModel, actualFieldModel);
    }

    @Test
    public void testGetFieldModelActionNull() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class))).thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(Mockito.eq(fieldModel))).thenReturn(null);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        Optional<FieldModel> actualFieldModel = configUtility.getFieldModel();
        assertFalse(actualFieldModel.isPresent());
    }

    @Test
    public void testSave() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(Mockito.eq(fieldModel))).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeSaveAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(apiAction.afterSaveAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.save(fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationModelConfigurationAccessor).createConfiguration(Mockito.eq(descriptorKey), Mockito.eq(ConfigContextEnum.GLOBAL), Mockito.anyCollection());
    }

    @Test
    public void testUpdateNoExistingConfig() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(Mockito.eq(fieldModel))).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeUpdateAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(apiAction.afterUpdateAction(Mockito.eq(fieldModel), Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.update(1L, fieldModel);

        assertEquals(fieldModel, savedModel);
    }

    @Test
    public void testUpdate() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.anyLong())).thenReturn(Optional.of(configurationModel));
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection())).thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(Mockito.eq(fieldModel))).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeUpdateAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(apiAction.afterUpdateAction(Mockito.eq(fieldModel), Mockito.eq(fieldModel))).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.update(1L, fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationModelConfigurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

}
