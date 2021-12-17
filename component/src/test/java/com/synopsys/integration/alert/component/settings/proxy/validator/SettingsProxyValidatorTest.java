package com.synopsys.integration.alert.component.settings.proxy.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;

public class SettingsProxyValidatorTest {
    private static final String HOST = "hostname";
    private static final Integer PORT = 12345;
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";

    SettingsProxyValidator settingsProxyValidator = new SettingsProxyValidator();

    @Test
    public void validateTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setProxyPassword(PASSWORD);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void validateNameMissingTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setProxyPassword(PASSWORD);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_CONFIGURATION_NAME));
    }

    @Test
    public void validateWithoutHostAndPortTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(2, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_HOST_FIELD_NAME));
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PORT_FIELD_NAME));
    }

    @Test
    public void validateHostWithoutPortTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PORT_FIELD_NAME));
    }

    @Test
    public void validatePortWithoutHostTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyPort(PORT);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_HOST_FIELD_NAME));
    }

    @Test
    public void validateUsernameWithoutPasswordTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_PASSWORD_FIELD_NAME));
    }

    @Test
    public void validatePasswordWithoutUsernameTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyPassword(PASSWORD);

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_USERNAME_FIELD_NAME));
    }

    @Test
    public void validateNonProxyHostsTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setNonProxyHosts(List.of("nonProxyHost"));

        ValidationResponseModel validationResponseModel = settingsProxyValidator.validate(settingsProxyModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(1, validationResponseModel.getErrors().size());
        assertTrue(validationResponseModel.getErrors().containsKey(SettingsProxyValidator.PROXY_HOST_FIELD_NAME));
    }
}
