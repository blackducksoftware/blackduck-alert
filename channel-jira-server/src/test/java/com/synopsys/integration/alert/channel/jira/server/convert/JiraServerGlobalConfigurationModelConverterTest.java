package com.synopsys.integration.alert.channel.jira.server.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

class JiraServerGlobalConfigurationModelConverterTest {
    public static final String TEST_URL = "http://test.jira.example.com";
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
        assertEquals(TEST_USERNAME, jiraModel.getUserName());
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
        assertEquals(TEST_USERNAME, jiraModel.getUserName());
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
