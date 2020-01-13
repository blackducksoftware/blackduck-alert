package com.synopsys.integration.alert.component.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsUIConfig;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.web.config.FieldValidationAction;

public class SettingsGlobalApiActionTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();
    private SettingsUIConfig settingsUIConfig;

    @BeforeEach
    public void initialize() {
        settingsUIConfig = new SettingsUIConfig();
    }

    @Test
    public void testReadConfig() {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        SettingsGlobalApiAction actionApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, settingsValidator);
        FieldModel afterGetAction = actionApi.afterGetAction(fieldModel);
        assertFieldsMissing(afterGetAction);
        Mockito.when(encryptionUtility.isPasswordSet()).thenReturn(true);
        Mockito.when(encryptionUtility.isGlobalSaltSet()).thenReturn(true);
        UserModel userModel = Mockito.mock(UserModel.class);
        Mockito.when(userModel.getPassword()).thenReturn("valid_test_value");
        Mockito.when(userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID)).thenReturn(Optional.of(userModel));
        FieldModel withFields = actionApi.afterGetAction(fieldModel);
        assertFieldsPresent(withFields);
    }

    @Test
    public void testUpdateConfig() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, settingsValidator);

        UserModel userModel = UserModel.existingUser(UserAccessor.DEFAULT_ADMIN_USER_ID, "example", null, null, AuthenticationType.DATABASE, Set.of(), true);
        Mockito.when(userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID)).thenReturn(Optional.of(userModel));

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        FieldModel handleNewAndUpdatedConfig = actionaApi.beforeUpdateAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfig() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, settingsValidator);

        UserModel userModel = UserModel.existingUser(UserAccessor.DEFAULT_ADMIN_USER_ID, "example", null, null, AuthenticationType.DATABASE, Set.of(), true);
        Mockito.when(userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID)).thenReturn(Optional.of(userModel));

        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        FieldModel handleNewAndUpdatedConfig = actionaApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfigEncryptionException() throws Exception {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = Mockito.mock(SettingsValidator.class);
        SettingsGlobalApiAction actionApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, settingsValidator);
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));

        Mockito.doThrow(new IllegalArgumentException()).when(encryptionUtility).updatePasswordField(Mockito.anyString());
        FieldModel handleNewAndUpdatedConfig = actionApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
    }

    @Test
    public void testValidateRequiredFieldsSet() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));
        HashMap<String, String> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateFieldsIsSetNoValue() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        HashMap<String, String> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateRequiredFieldsMissing() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));
        HashMap<String, String> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));

        fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldErrors.clear();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    @Test
    public void testValidateFieldsIsSetFalseHasValue() {
        FieldModel fieldModel = new FieldModel(SETTINGS_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("    "), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("    "), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("      "), false));
        HashMap<String, String> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(settingsUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    private void assertFieldsMissing(FieldModel fieldModel) {
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).isPresent());
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).map(FieldValueModel::isSet).orElse(false));
        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).map(FieldValueModel::isSet).orElse(false));
        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).map(FieldValueModel::isSet).orElse(false));
    }

    private void assertFieldsPresent(FieldModel fieldModel) {
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).isPresent());
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> Optional.of(field.isSet())).orElse(false));
    }
}
