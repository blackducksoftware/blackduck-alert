package com.synopsys.integration.alert.component.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

public class SettingsDescriptorActionApiTest {

    private final SettingsUIConfig settingsUIConfig = new SettingsUIConfig();

    @Test
    public void testTestConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);

        actionaApi.testConfig(settingsUIConfig.createFields(), null);
        Mockito.verify(encryptionUtility, Mockito.times(0)).isInitialized();
        Mockito.verify(userAccessor, Mockito.times(0)).getUser(Mockito.anyString());
        Mockito.verify(systemValidator, Mockito.times(0)).validate(Mockito.anyMap());
    }

    @Test
    public void testReadConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        FieldModel fieldModel = Mockito.mock(FieldModel.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);

        actionaApi.readConfig(fieldModel);
        assertFieldsMissing(fieldModel);
        fieldModel = Mockito.mock(FieldModel.class);
        Mockito.when(encryptionUtility.isPasswordSet()).thenReturn(true);
        Mockito.when(encryptionUtility.isGlobalSaltSet()).thenReturn(true);
        final UserModel userModel = Mockito.mock(UserModel.class);
        Mockito.when(userModel.getPassword()).thenReturn("valid_test_value");
        Mockito.when(userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER)).thenReturn(Optional.of(userModel));
        final FieldModel modelCreated = actionaApi.readConfig(fieldModel);
        assertFieldsPresent(modelCreated);
    }

    @Test
    public void testUpdateConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        final FieldModel modelToSave = actionaApi.updateConfig(fieldModel);
        assertFieldsMissing(modelToSave);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        final FieldModel modelToSave = actionaApi.saveConfig(fieldModel);
        assertFieldsMissing(modelToSave);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
        Mockito.verify(systemValidator).validate();
    }

    @Test
    public void testSaveConfigEncryptionException() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));

        Mockito.doThrow(new IllegalArgumentException()).when(encryptionUtility).updatePasswordField(Mockito.anyString());
        final FieldModel modelToSave = actionaApi.saveConfig(fieldModel);
        assertFieldsMissing(modelToSave);
    }

    @Test
    public void testValidateRequiredFieldsSet() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateFieldsIsSetNoValue() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(null, true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(null, true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, true));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateRequiredFieldsMissing() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));
        HashMap<String, String> fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));

        fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    @Test
    public void testValidateFieldsIsSetFalseHasValue() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(List.of("    "), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(List.of("    "), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("      "), true));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD));
        assertEquals(DescriptorActionApi.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    @Test
    public void testLdapEnabled() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SettingsDescriptorActionApi actionaApi = new SettingsDescriptorActionApi(encryptionUtility, userAccessor, systemValidator);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD, new FieldValueModel(null, true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD, new FieldValueModel(null, true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(null, true));
        fieldModel.putField(SettingsDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), false));
        fieldModel.putField(SettingsDescriptor.KEY_LDAP_SERVER, new FieldValueModel(List.of(""), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        actionaApi.validateConfig(settingsUIConfig.createFields(), fieldModel, fieldErrors);
        assertFalse(fieldErrors.isEmpty());
        assertEquals(fieldErrors.get(SettingsDescriptor.KEY_LDAP_SERVER), SettingsDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING);
    }

    private void assertFieldsMissing(final FieldModel fieldModel) {
        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).isPresent());
        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).isPresent());
        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertFalse(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> Optional.of(field.isSet())).orElse(false));
    }

    private void assertFieldsPresent(final FieldModel fieldModel) {
        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).isPresent());
        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).isPresent());
        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PASSWORD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_PASSWORD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> Optional.of(field.isSet())).orElse(false));
    }
}
