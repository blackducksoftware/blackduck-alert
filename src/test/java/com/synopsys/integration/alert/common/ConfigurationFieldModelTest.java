package com.synopsys.integration.alert.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

public class ConfigurationFieldModelTest {

    public static final String KEY_FIELD_1 = "field_1";
    public static final String KEY_FIELD_2 = "field_2";
    public static final String VALUE_FIELD_1 = "value_1";
    public static final String VALUE_FIELD_2 = "value_2";

    private FieldModel createFieldModel() {
        final Map<String, FieldValueModel> valueModelMap = Map.of(KEY_FIELD_1, new FieldValueModel(List.of(VALUE_FIELD_1), false),
            KEY_FIELD_2, new FieldValueModel(List.of(VALUE_FIELD_2), false));
        return new FieldModel("descriptor", "GLOBAL", valueModelMap);
    }

    private Map<String, ConfigField> createConfigFieldMap() {
        return Map.of(KEY_FIELD_1, TextInputConfigField.create(KEY_FIELD_1, KEY_FIELD_1),
            KEY_FIELD_2, PasswordConfigField.create(KEY_FIELD_2, KEY_FIELD_2));
    }

    @Test
    public void convertToFieldAccessorTest() {
        final FieldModel fieldModel = createFieldModel();
        final Map<String, ConfigField> configFieldMap = createConfigFieldMap();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final FieldAccessor accessor = modelConverter.convertToFieldAccessor(configFieldMap, fieldModel);

        assertEquals(VALUE_FIELD_1, accessor.getString(KEY_FIELD_1).orElseThrow(IllegalArgumentException::new));
        assertEquals(VALUE_FIELD_2, accessor.getString(KEY_FIELD_2).orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void convertFromFieldModelTest() {
        final FieldModel fieldModel = createFieldModel();
        final Map<String, ConfigField> configFieldMap = createConfigFieldMap();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final Map<String, ConfigurationFieldModel> actualModelMap = modelConverter.convertFromFieldModel(configFieldMap, fieldModel);
        assertTrue(actualModelMap.containsKey(KEY_FIELD_1));
        assertTrue(actualModelMap.containsKey(KEY_FIELD_2));
        assertEquals(VALUE_FIELD_1, actualModelMap.get(KEY_FIELD_1).getFieldValue().orElseThrow(IllegalArgumentException::new));
        assertEquals(VALUE_FIELD_2, actualModelMap.get(KEY_FIELD_2).getFieldValue().orElseThrow(IllegalArgumentException::new));
    }

    @Test
    public void convertFromFieldModelEmptyFieldsTest() {
        final FieldModel fieldModel = createFieldModel();
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final Map<String, ConfigurationFieldModel> actualModelMap = modelConverter.convertFromFieldModel(Map.of(), fieldModel);
        assertTrue(actualModelMap.isEmpty());
    }

    @Test
    public void convertDefinedFieldModelTest() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, false), VALUE_FIELD_1);

        assertTrue(optionalModel.isPresent());
        ConfigurationFieldModel actualModel = optionalModel.get();
        assertEquals(VALUE_FIELD_1, actualModel.getFieldValue().orElseThrow(IllegalArgumentException::new));
        assertFalse(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());

        optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), VALUE_FIELD_1);

        assertTrue(optionalModel.isPresent());
        actualModel = optionalModel.get();
        assertEquals(VALUE_FIELD_1, actualModel.getFieldValue().orElseThrow(IllegalArgumentException::new));
        assertTrue(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());
    }

    @Test
    public void convertDefinedFieldModelEmptyTest() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(false);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), VALUE_FIELD_1);
        assertTrue(optionalModel.isEmpty());
    }

    @Test
    public void convertDefinedFieldModelWithValuesTest() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, false), List.of(VALUE_FIELD_1, VALUE_FIELD_2));

        assertTrue(optionalModel.isPresent());
        ConfigurationFieldModel actualModel = optionalModel.get();
        assertTrue(actualModel.getFieldValues().containsAll(List.of(VALUE_FIELD_1, VALUE_FIELD_2)));
        assertFalse(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());

        optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), List.of(VALUE_FIELD_1, VALUE_FIELD_2));

        assertTrue(optionalModel.isPresent());
        actualModel = optionalModel.get();
        assertTrue(actualModel.getFieldValues().containsAll(List.of(VALUE_FIELD_1, VALUE_FIELD_2)));
        assertTrue(actualModel.isSensitive());
        assertEquals(KEY_FIELD_1, actualModel.getFieldKey());
    }

    @Test
    public void convertDefinedFieldModelWithValuesEmptyTest() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(false);
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter(encryptionUtility);
        final Optional<ConfigurationFieldModel> optionalModel = modelConverter.convertFromDefinedFieldModel(new DefinedFieldModel(KEY_FIELD_1, ConfigContextEnum.GLOBAL, true), List.of(VALUE_FIELD_1, VALUE_FIELD_2));
        assertTrue(optionalModel.isEmpty());
    }
}
