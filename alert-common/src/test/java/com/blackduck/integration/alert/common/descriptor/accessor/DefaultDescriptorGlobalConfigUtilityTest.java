/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ApiAction;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

class DefaultDescriptorGlobalConfigUtilityTest {

    private DescriptorKey createDescriptorKey() {
        return new DescriptorKey("universal_key", "Universal Key") {};
    }

    @Test
    void testGetKey() {
        DescriptorKey descriptorKey = createDescriptorKey();
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, null, null, null);
        assertEquals(descriptorKey, configUtility.getKey());
    }

    @Test
    void testConfigurationExists() {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, null);
        assertFalse(configUtility.doesConfigurationExist());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(configurationModel));

        assertTrue(configUtility.doesConfigurationExist());
    }

    @Test
    void testGetConfiguration() {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, null);
        assertFalse(configUtility.getConfiguration().isPresent());
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(configurationModel));

        assertTrue(configUtility.getConfiguration().isPresent());
    }

    @Test
    void testGetFieldModelEmptyConfiguration() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, null, converter);
        assertFalse(configUtility.getFieldModel().isPresent());
    }

    @Test
    void testGetFieldModel() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(fieldModel)).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel actualFieldModel = configUtility.getFieldModel().orElse(null);
        assertEquals(fieldModel, actualFieldModel);
    }

    @Test
    void testGetFieldModelActionNull() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(DescriptorKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(configurationModel));
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.afterGetAction(fieldModel)).thenReturn(null);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        Optional<FieldModel> actualFieldModel = configUtility.getFieldModel();
        assertFalse(actualFieldModel.isPresent());
    }

    @Test
    void testSave() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection()))
            .thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(fieldModel)).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeSaveAction(fieldModel)).thenReturn(fieldModel);
        Mockito.when(apiAction.afterSaveAction(fieldModel)).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.save(fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationModelConfigurationAccessor).createConfiguration(Mockito.eq(descriptorKey), Mockito.eq(ConfigContextEnum.GLOBAL), Mockito.anyCollection());
    }

    @Test
    void testUpdateNoExistingConfig() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection()))
            .thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(fieldModel)).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeUpdateAction(fieldModel)).thenReturn(fieldModel);
        Mockito.when(apiAction.afterUpdateAction(fieldModel, fieldModel)).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.update(1L, fieldModel);

        assertEquals(fieldModel, savedModel);
    }

    @Test
    void testUpdate() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey();
        FieldModel fieldModel = new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), Map.of());
        Map<String, ConfigurationFieldModel> configurationFieldModelCollection = Map.of();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        ConfigurationFieldModelConverter converter = Mockito.mock(ConfigurationFieldModelConverter.class);
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ApiAction apiAction = Mockito.mock(ApiAction.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.anyLong())).thenReturn(Optional.of(configurationModel));
        Mockito.when(configurationModelConfigurationAccessor.createConfiguration(Mockito.eq(descriptorKey), Mockito.any(ConfigContextEnum.class), Mockito.anyCollection()))
            .thenReturn(configurationModel);
        Mockito.when(converter.convertToConfigurationFieldModelMap(fieldModel)).thenReturn(configurationFieldModelCollection);
        Mockito.when(converter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(apiAction.beforeUpdateAction(fieldModel)).thenReturn(fieldModel);
        Mockito.when(apiAction.afterUpdateAction(fieldModel, fieldModel)).thenReturn(fieldModel);

        DefaultDescriptorGlobalConfigUtility configUtility = new DefaultDescriptorGlobalConfigUtility(descriptorKey, configurationModelConfigurationAccessor, apiAction, converter);
        FieldModel savedModel = configUtility.update(1L, fieldModel);

        assertEquals(fieldModel, savedModel);
        Mockito.verify(configurationModelConfigurationAccessor).updateConfiguration(Mockito.anyLong(), Mockito.anyCollection());
    }

}
