package com.blackduck.integration.alert.component.settings.proxy.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.util.DateUtils;

class SettingsProxyValidatorTest {
    private static final String ID = UUID.randomUUID().toString();
    private static final String CREATED_AT = DateUtils.createCurrentDateTimestamp().toString();
    private static final String LAST_UPDATED = DateUtils.createCurrentDateTimestamp().toString();
    private static final String HOST = "hostname";
    private static final Integer PORT = 12345;
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";
    private static final List<String> NON_PROXY_HOSTS = List.of("nonProxyHost");

    SettingsProxyValidator settingsProxyValidator = new SettingsProxyValidator();

    @Test
    void validateTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            USERNAME,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validateNameMissingTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            null,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            USERNAME,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_CONFIGURATION_NAME));
    }

    @Test
    void validateWithoutHostAndPortTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            null,
            null,
            USERNAME,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(2, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_HOST_FIELD_NAME));
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PORT_FIELD_NAME));
    }

    @Test
    void validateHostWithoutPortTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            null,
            USERNAME,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PORT_FIELD_NAME));
    }

    @Test
    void validatePortWithoutHostTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            null,
            PORT,
            USERNAME,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_HOST_FIELD_NAME));
    }

    @Test
    void validateUsernameWithoutPasswordTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            USERNAME,
            null,
            Boolean.FALSE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PASSWORD_FIELD_NAME));
    }

    @Test
    void validateUsernameWithoutPasswordIsProxyPasswordSetTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            USERNAME,
            null,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validatePasswordWithoutUsernameTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            null,
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_USERNAME_FIELD_NAME));
    }

    @Test
    void validateIsPasswordSetWithoutUsernameTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            null,
            null,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_USERNAME_FIELD_NAME));
    }

    @Test
    void validateUsernameBlank() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(
            ID,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            LAST_UPDATED,
            HOST,
            PORT,
            "",
            PASSWORD,
            Boolean.TRUE,
            NON_PROXY_HOSTS
        );

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertFalse(validationResponseModel.hasErrors());
    }
}
