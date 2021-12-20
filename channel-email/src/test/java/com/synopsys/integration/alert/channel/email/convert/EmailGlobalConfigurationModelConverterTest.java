package com.synopsys.integration.alert.channel.email.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

class EmailGlobalConfigurationModelConverterTest {
    public static final String TEST_AUTH_REQUIRED = "true";
    public static final String TEST_FROM = "test.user@some.company.example.com";
    public static final String TEST_SMTP_HOST = "stmp.server.example.com";
    public static final String TEST_AUTH_PASSWORD = "apassword";
    public static final String TEST_SMTP_PORT = "2025";
    public static final String TEST_AUTH_USER = "auser";

    @Test
    void validConversionTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        EmailGlobalConfigurationModelConverter converter = new EmailGlobalConfigurationModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(configurationModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        assertEquals(Boolean.TRUE, emailModel.getSmtpAuth().orElse(Boolean.FALSE));
        assertEquals(TEST_AUTH_USER, emailModel.getSmtpUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, emailModel.getSmtpPassword().orElse(null));
        assertEquals(TEST_SMTP_HOST, emailModel.getSmtpHost().orElse(null));
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), emailModel.getSmtpPort().orElse(null));
        assertEquals(TEST_FROM, emailModel.getSmtpFrom().orElse(null));

        Map<String, String> additionalProperties = emailModel.getAdditionalJavaMailProperties().orElse(Map.of());
        assertEquals(1, additionalProperties.size());
        assertEquals("true", additionalProperties.get(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()));

    }

    @Test
    void invalidPortTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        configurationModel.getField(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey())
            .ifPresent(field -> field.setFieldValue("twenty-five"));
        EmailGlobalConfigurationModelConverter converter = new EmailGlobalConfigurationModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(configurationModel);
        assertTrue(model.isEmpty());
    }

    @Test
    void emptyFieldsTest() {
        ConfigurationModel emptyModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, Map.of());
        EmailGlobalConfigurationModelConverter converter = new EmailGlobalConfigurationModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(emptyModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        assertTrue(emailModel.getSmtpAuth().isEmpty());
        assertTrue(emailModel.getSmtpUsername().isEmpty());
        assertTrue(emailModel.getSmtpPassword().isEmpty());
        assertTrue(emailModel.getSmtpHost().isEmpty());
        assertTrue(emailModel.getSmtpPort().isEmpty());
        assertTrue(emailModel.getSmtpFrom().isEmpty());
        assertTrue(emailModel.getAdditionalJavaMailProperties().isPresent());
        assertTrue(emailModel.getAdditionalJavaMailProperties().orElseThrow(() -> new AssertionError("Expected an additional properties map.")).isEmpty());
    }

    @Test
    void invalidEmailPropertyKeysTest() {
        String invalidFieldKey = "invalid.email.field";
        ConfigurationFieldModel invalidField = ConfigurationFieldModel.create(invalidFieldKey);
        Map<String, ConfigurationFieldModel> fieldValues = Map.of(invalidFieldKey, invalidField);
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValues);
        EmailGlobalConfigurationModelConverter converter = new EmailGlobalConfigurationModelConverter();
        Optional<EmailGlobalConfigModel> model = converter.convert(configurationModel);
        assertTrue(model.isPresent());
        EmailGlobalConfigModel emailModel = model.get();
        Map<String, String> additionalProperties = emailModel.getAdditionalJavaMailProperties().orElse(Map.of());
        assertEquals(0, additionalProperties.size());

    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel fromField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());
        ConfigurationFieldModel hostField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        ConfigurationFieldModel portField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey());
        ConfigurationFieldModel authField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey());
        ConfigurationFieldModel passwordField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey());
        ConfigurationFieldModel userField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey());

        ConfigurationFieldModel ehloField = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey());
        fromField.setFieldValue(TEST_FROM);
        hostField.setFieldValue(TEST_SMTP_HOST);
        portField.setFieldValue(TEST_SMTP_PORT);
        authField.setFieldValue(TEST_AUTH_REQUIRED);
        passwordField.setFieldValue(TEST_AUTH_PASSWORD);
        userField.setFieldValue(TEST_AUTH_USER);
        ehloField.setFieldValue("true");
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), fromField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), hostField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), portField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), authField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), passwordField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), userField);
        fieldValuesMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), ehloField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }

}
