package com.synopsys.integration.alert.component.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsUIConfig;
import com.synopsys.integration.alert.util.AlertFieldStatusConverter;
import com.synopsys.integration.alert.web.common.field.FieldValidationAction;

public class SettingsGlobalApiActionTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();
    private SettingsUIConfig settingsUIConfig;

    @BeforeEach
    public void initialize() {
        settingsUIConfig = new SettingsUIConfig();
        settingsUIConfig.setConfigFields();
    }

    @Test
    public void testReadConfig() {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        SettingsGlobalApiAction actionApi = new SettingsGlobalApiAction(encryptionUtility, settingsValidator);
        FieldModel afterGetAction = actionApi.afterGetAction(fieldModel);
        assertFieldsMissing(afterGetAction);
        Mockito.when(encryptionUtility.isPasswordSet()).thenReturn(true);
        Mockito.when(encryptionUtility.isGlobalSaltSet()).thenReturn(true);
        UserModel userModel = Mockito.mock(UserModel.class);
        Mockito.when(userModel.getPassword()).thenReturn("valid_test_value");
        FieldModel withFields = actionApi.afterGetAction(fieldModel);
        assertFieldsPresent(withFields);
    }

    @Test
    public void testUpdateConfig() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, settingsValidator);

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        FieldModel handleNewAndUpdatedConfig = actionaApi.beforeUpdateAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfig() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, settingsValidator);

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        FieldModel handleNewAndUpdatedConfig = actionaApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfigEncryptionException() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionApi = new SettingsGlobalApiAction(encryptionUtility, settingsValidator);
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));

        Mockito.doThrow(new IllegalArgumentException()).when(encryptionUtility).updatePasswordField(Mockito.anyString());
        FieldModel handleNewAndUpdatedConfig = actionApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
    }

    @Test
    public void testValidateRequiredFieldsSet() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateFieldsIsSetNoValue() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateRequiredFieldsMissing() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> initialFieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);
        Map<String, AlertFieldStatus> fieldErrorsMap = AlertFieldStatusConverter.convertToMap(initialFieldErrors);

        assertFalse(fieldErrorsMap.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_PWD).getFieldMessage());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).getFieldMessage());

        fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldErrorsMap.clear();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        fieldErrorsMap = AlertFieldStatusConverter.convertToMap(fieldErrors);

        assertFalse(fieldErrorsMap.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_PWD).getFieldMessage());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).getFieldMessage());
    }

    @Test
    public void testValidateFieldsIsSetFalseHasValue() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("    "), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("      "), false));
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, fieldModel);

        Map<String, AlertFieldStatus> fieldErrorsMap = AlertFieldStatusConverter.convertToMap(fieldErrors);

        assertFalse(fieldErrorsMap.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_PWD).getFieldMessage());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrorsMap.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).getFieldMessage());
    }

    private void assertFieldsMissing(FieldModel fieldModel) {
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).map(FieldValueModel::isSet).orElse(false));
        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).map(FieldValueModel::isSet).orElse(false));
    }

    private void assertFieldsPresent(FieldModel fieldModel) {
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> Optional.of(field.isSet())).orElse(false));
    }

}
