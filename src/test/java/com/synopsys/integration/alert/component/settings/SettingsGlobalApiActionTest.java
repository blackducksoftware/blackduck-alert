package com.synopsys.integration.alert.component.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsUIConfig;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.web.config.FieldValidationAction;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLManager;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

public class SettingsGlobalApiActionTest {

    private final SettingsUIConfig settingsUIConfig = new SettingsUIConfig();

    @Test
    public void testReadConfig() {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);

        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        final SettingsGlobalApiAction actionApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, systemValidator, samlManager);
        final FieldModel afterGetAction = actionApi.afterGetAction(fieldModel);
        assertFieldsMissing(afterGetAction);
        Mockito.when(encryptionUtility.isPasswordSet()).thenReturn(true);
        Mockito.when(encryptionUtility.isGlobalSaltSet()).thenReturn(true);
        final UserModel userModel = Mockito.mock(UserModel.class);
        Mockito.when(userModel.getPassword()).thenReturn("valid_test_value");
        Mockito.when(userAccessor.getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER)).thenReturn(Optional.of(userModel));
        final FieldModel withFields = actionApi.afterGetAction(fieldModel);
        assertFieldsPresent(withFields);
    }

    @Test
    public void testUpdateConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, systemValidator, samlManager);

        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        final FieldModel handleNewAndUpdatedConfig = actionaApi.beforeUpdateAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
    }

    @Test
    public void testSaveConfig() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, systemValidator, samlManager);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));

        final FieldModel handleNewAndUpdatedConfig = actionaApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
        Mockito.verify(userAccessor).changeUserPassword(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(encryptionUtility).updatePasswordField(Mockito.anyString());
        Mockito.verify(encryptionUtility).updateSaltField(Mockito.anyString());
        Mockito.verify(systemValidator).validate();
    }

    @Test
    public void testSaveConfigEncryptionException() throws Exception {
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        final SystemValidator systemValidator = Mockito.mock(SystemValidator.class);
        final SAMLManager samlManager = Mockito.mock(SAMLManager.class);
        final SettingsGlobalApiAction actionaApi = new SettingsGlobalApiAction(encryptionUtility, userAccessor, systemValidator, samlManager);
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));

        Mockito.doThrow(new IllegalArgumentException()).when(encryptionUtility).updatePasswordField(Mockito.anyString());
        final FieldModel handleNewAndUpdatedConfig = actionaApi.beforeSaveAction(fieldModel);
        assertFieldsMissing(handleNewAndUpdatedConfig);
    }

    @Test
    public void testValidateRequiredFieldsSet() {
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("valid_test_value"), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("valid_test_value"), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = settingsUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateFieldsIsSetNoValue() {
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = settingsUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testValidateRequiredFieldsMissing() {
        FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(""), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(""), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = settingsUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));

        fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldErrors.clear();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    @Test
    public void testValidateFieldsIsSetFalseHasValue() {
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of("    "), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of("    "), false));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of("      "), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = settingsUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);

        assertFalse(fieldErrors.isEmpty());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_PWD));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT));
    }

    @Test
    public void testLdapEnabled() {
        final FieldModel fieldModel = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_PWD, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, new FieldValueModel(List.of(), true));
        fieldModel.putField(SettingsDescriptor.KEY_LDAP_ENABLED, new FieldValueModel(List.of("true"), false));
        fieldModel.putField(SettingsDescriptor.KEY_LDAP_SERVER, new FieldValueModel(List.of(""), false));
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = settingsUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertFalse(fieldErrors.isEmpty());
        assertEquals(SettingsDescriptor.FIELD_ERROR_LDAP_SERVER_MISSING, fieldErrors.get(SettingsDescriptor.KEY_LDAP_SERVER));
    }

    private void assertFieldsMissing(final FieldModel fieldModel) {
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).isPresent());
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertFalse(fieldModel.getFieldValue(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).map(FieldValueModel::isSet).orElse(false));
        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).map(FieldValueModel::isSet).orElse(false));
        assertFalse(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).map(FieldValueModel::isSet).orElse(false));
    }

    private void assertFieldsPresent(final FieldModel fieldModel) {
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).isPresent());
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).isPresent());
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).isPresent());

        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_PWD).flatMap(field -> Optional.of(field.isSet())).orElse(false));
        assertTrue(fieldModel.getFieldValueModel(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT).flatMap(field -> Optional.of(field.isSet())).orElse(false));
    }
}
