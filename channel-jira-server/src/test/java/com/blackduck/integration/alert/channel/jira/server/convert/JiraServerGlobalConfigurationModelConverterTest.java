/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;

class JiraServerGlobalConfigurationModelConverterTest {
    public static final String TEST_URL = "http://test.jira.example.com";
    public static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "testpassword";
    public static final String TEST_DISABLE_PLUGIN_CHECK = "true";

    private JiraServerGlobalConfigurationValidator validator;

    @BeforeEach
    public void init() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
    }

    @Test
    void validConversionTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        JiraServerGlobalConfigurationModelConverter converter = new JiraServerGlobalConfigurationModelConverter(validator);
        Optional<JiraServerGlobalConfigModel> model = converter.convertAndValidate(configurationModel, null);
        assertTrue(model.isPresent());
        JiraServerGlobalConfigModel jiraModel = model.get();

        assertNull(jiraModel.getId());
        assertEquals(TEST_URL, jiraModel.getUrl());
        assertEquals(TEST_USERNAME, jiraModel.getUserName().orElse("Username missing"));
        assertEquals(TEST_PASSWORD, jiraModel.getPassword().orElse("Password value is missing"));
        assertTrue(jiraModel.getDisablePluginCheck().orElse(Boolean.FALSE));

    }

    @Test
    void validConversionWithExistingConfigTest() {
        String uuid = UUID.randomUUID().toString();
        ConfigurationModel configurationModel = createDefaultConfigurationModel();

        JiraServerGlobalConfigModel jiraServerGlobalConfigModelSaved = new JiraServerGlobalConfigModel(
            uuid,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            TEST_URL,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModelSaved.setUserName(TEST_USERNAME);
        jiraServerGlobalConfigModelSaved.setPassword(TEST_PASSWORD);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(jiraServerGlobalConfigModelSaved));
        validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);

        JiraServerGlobalConfigurationModelConverter converter = new JiraServerGlobalConfigurationModelConverter(validator);
        Optional<JiraServerGlobalConfigModel> model = converter.convertAndValidate(configurationModel, uuid);

        assertTrue(model.isPresent());
        JiraServerGlobalConfigModel jiraModel = model.get();

        assertEquals(TEST_URL, jiraModel.getUrl());
        assertEquals(TEST_USERNAME, jiraModel.getUserName().orElse("Username missing"));
        assertEquals(TEST_PASSWORD, jiraModel.getPassword().orElse("Password value is missing"));
        assertTrue(jiraModel.getDisablePluginCheck().orElse(Boolean.FALSE));
    }

    @Test
    void validConversionMissingOptionalFieldsTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        Map<String, ConfigurationFieldModel> fields = configurationModel.getCopyOfKeyToFieldMap();
        fields.remove(JiraServerGlobalConfigurationModelConverter.DISABLE_PLUGIN_CHECK_KEY);
        configurationModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fields);
        JiraServerGlobalConfigurationModelConverter converter = new JiraServerGlobalConfigurationModelConverter(validator);
        Optional<JiraServerGlobalConfigModel> model = converter.convertAndValidate(configurationModel, null);
        assertTrue(model.isPresent());
        JiraServerGlobalConfigModel jiraModel = model.get();

        assertNull(jiraModel.getId());
        assertEquals(TEST_URL, jiraModel.getUrl());
        assertEquals(TEST_USERNAME, jiraModel.getUserName().orElse("Username missing"));
        assertEquals(TEST_PASSWORD, jiraModel.getPassword().orElse("Password value is missing"));
        assertTrue(jiraModel.getDisablePluginCheck().isEmpty());

    }

    @Test
    void emptyFieldsTest() {
        ConfigurationModel emptyModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, Map.of());
        JiraServerGlobalConfigurationModelConverter converter = new JiraServerGlobalConfigurationModelConverter(validator);
        Optional<JiraServerGlobalConfigModel> model = converter.convertAndValidate(emptyModel, null);
        assertTrue(model.isEmpty());
    }

    @Test
    void invalidPropertyKeysTest() {
        String invalidFieldKey = "invalid.jira.field";
        ConfigurationFieldModel invalidField = ConfigurationFieldModel.create(invalidFieldKey);
        Map<String, ConfigurationFieldModel> fieldValues = Map.of(invalidFieldKey, invalidField);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValues);
        JiraServerGlobalConfigurationModelConverter converter = new JiraServerGlobalConfigurationModelConverter(validator);
        Optional<JiraServerGlobalConfigModel> model = converter.convertAndValidate(configurationModel, null);
        assertTrue(model.isEmpty());
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel urlField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.URL_KEY);
        ConfigurationFieldModel userField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.USERNAME_KEY);
        ConfigurationFieldModel passwordField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY);
        ConfigurationFieldModel disableCheckField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.DISABLE_PLUGIN_CHECK_KEY);

        urlField.setFieldValue(TEST_URL);
        passwordField.setFieldValue(TEST_PASSWORD);
        userField.setFieldValue(TEST_USERNAME);
        disableCheckField.setFieldValue(TEST_DISABLE_PLUGIN_CHECK);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.URL_KEY, urlField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY, passwordField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.USERNAME_KEY, userField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.DISABLE_PLUGIN_CHECK_KEY, disableCheckField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }
}
