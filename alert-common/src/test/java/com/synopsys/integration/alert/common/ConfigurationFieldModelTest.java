package com.synopsys.integration.alert.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.mock.MockDescriptorAccessor;

public class ConfigurationFieldModelTest {
    public static final String KEY_FIELD_1 = "field_1";
    public static final String KEY_FIELD_2 = "field_2";
    public static final String VALUE_FIELD_1 = "value_1";
    public static final String VALUE_FIELD_2 = "value_2";

    private FieldModel createFieldModel() {
        Map<String, FieldValueModel> valueModelMap = Map.of(KEY_FIELD_1, new FieldValueModel(List.of(VALUE_FIELD_1), false),
            KEY_FIELD_2, new FieldValueModel(List.of(VALUE_FIELD_2), false));
        return new FieldModel("descriptor", "GLOBAL", valueModelMap);
    }

    private List<DefinedFieldModel> createConfigFields() {
        return List.of(
            DefinedFieldModel.createGlobalField(KEY_FIELD_1),
            DefinedFieldModel.createGlobalSensitiveField(KEY_FIELD_2)
        );
    }

    private List<DescriptorKey> createDescriptorKeyList() throws AlertException {
        DescriptorKey descriptorKey = new DescriptorKey("descriptor", "descriptor") {};
        return List.of(descriptorKey);
    }

    @Test
    public void convertToFieldAccessorTest() throws Exception {
        FieldModel fieldModel = createFieldModel();
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        FieldUtility accessor = modelConverter.convertToFieldAccessor(fieldModel);

        assertEquals(VALUE_FIELD_1, accessor.getString(KEY_FIELD_1).orElse(null));
        assertEquals(VALUE_FIELD_2, accessor.getString(KEY_FIELD_2).orElse(null));
    }

    @Test
    public void convertFromFieldModelTest() throws Exception {
        FieldModel fieldModel = createFieldModel();
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(Boolean.TRUE);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Map<String, ConfigurationFieldModel> actualModelMap = modelConverter.convertToConfigurationFieldModelMap(fieldModel);
        assertTrue(actualModelMap.containsKey(KEY_FIELD_1));
        assertTrue(actualModelMap.containsKey(KEY_FIELD_2));
        assertEquals(VALUE_FIELD_1, actualModelMap.get(KEY_FIELD_1).getFieldValue().orElse(null));
        assertEquals(VALUE_FIELD_2, actualModelMap.get(KEY_FIELD_2).getFieldValue().orElse(null));
    }

    @Test
    public void convertFromFieldModelEmptyFieldsTest() throws Exception {
        FieldModel fieldModel = createFieldModel();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(List.of());
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Map<String, ConfigurationFieldModel> actualModelMap = modelConverter.convertToConfigurationFieldModelMap(fieldModel);
        assertTrue(actualModelMap.isEmpty());
    }

    @Test
    public void convertDefinedFieldModelTest() throws Exception {
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, false), VALUE_FIELD_1, true);

        assertTrue(optionalModel.isPresent());
        ConfigurationFieldModel actualModel = optionalModel.get();
        assertEquals(VALUE_FIELD_1, actualModel.getFieldValue().orElseThrow(IllegalArgumentException::new));
        assertFalse(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());

        optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), VALUE_FIELD_1, true);

        assertTrue(optionalModel.isPresent());
        actualModel = optionalModel.get();
        assertEquals(VALUE_FIELD_1, actualModel.getFieldValue().orElseThrow(IllegalArgumentException::new));
        assertTrue(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());
    }

    @Test
    public void convertDefinedFieldModelEmptyTest() throws Exception {
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), VALUE_FIELD_1, true);
        assertTrue(optionalModel.isEmpty());
    }

    @Test
    public void convertDefinedFieldModelWithValuesTest() throws Exception {
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, false), List.of(VALUE_FIELD_1, VALUE_FIELD_2), true);

        assertTrue(optionalModel.isPresent());
        ConfigurationFieldModel actualModel = optionalModel.get();
        assertTrue(actualModel.getFieldValues().containsAll(List.of(VALUE_FIELD_1, VALUE_FIELD_2)));
        assertFalse(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());

        optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), List.of(VALUE_FIELD_1, VALUE_FIELD_2), true);

        assertTrue(optionalModel.isPresent());
        actualModel = optionalModel.get();
        assertTrue(actualModel.getFieldValues().containsAll(List.of(VALUE_FIELD_1, VALUE_FIELD_2)));
        assertTrue(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());
    }

    @Test
    public void convertDefinedFieldModelWithValuesEmptyTest() throws Exception {
        List<DefinedFieldModel> configFields = createConfigFields();
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DescriptorAccessor descriptorAccessor = new MockDescriptorAccessor(configFields);
        List<DescriptorKey> descriptorKeys = createDescriptorKeyList();
        ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility, descriptorAccessor, descriptorKeys);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), List.of(VALUE_FIELD_1, VALUE_FIELD_2), true);
        assertTrue(optionalModel.isEmpty());
    }

}
