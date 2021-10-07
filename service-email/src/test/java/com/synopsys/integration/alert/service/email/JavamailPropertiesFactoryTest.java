package com.synopsys.integration.alert.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class JavamailPropertiesFactoryTest {
    public static final String EXPECTED_FROM_VALUE = "expectedFrom";
    public static final String EXPECTED_HOST_VALUE = "expectedHost";
    public static final int EXPECTED_PORT_VALUE = 25;
    public static final String EXPECTED_PORT_VALUE_STRING = "25";
    public static final Boolean EXPECTED_AUTH_VALUE = Boolean.TRUE;
    public static final String EXPECTED_AUTH_VALUE_STRING = "true";
    public static final String EXPECTED_USERNAME_VALUE = "expectedUsername";
    public static final String EXPECTED_PASSWORD_VALUE = "expectedPassword";
    public static final String EXPECTED_EHLO_VALUE = "expectedEhlo";


    @Test
    public void testCreateFromEmailGlobalConfigModel() {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setFrom(EXPECTED_FROM_VALUE);
        emailGlobalConfigModel.setHost(EXPECTED_HOST_VALUE);
        emailGlobalConfigModel.setPort(EXPECTED_PORT_VALUE);
        emailGlobalConfigModel.setAuth(EXPECTED_AUTH_VALUE);
        emailGlobalConfigModel.setUsername(EXPECTED_USERNAME_VALUE);
        emailGlobalConfigModel.setPassword(EXPECTED_PASSWORD_VALUE);
        emailGlobalConfigModel.setAdditionalJavaMailProperties(Map.of(
            EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), EXPECTED_EHLO_VALUE
        ));

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();

        Properties properties = javamailPropertiesFactory.createJavaMailProperties(emailGlobalConfigModel);

        assertEquals(EXPECTED_FROM_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
        assertEquals(EXPECTED_HOST_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        assertEquals(EXPECTED_PORT_VALUE_STRING, properties.getProperty(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        assertEquals(EXPECTED_AUTH_VALUE_STRING, properties.getProperty(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()));
        assertEquals(EXPECTED_USERNAME_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey()));
        assertEquals(EXPECTED_EHLO_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()));
        assertNull(properties.getProperty(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()));
    }

    @Test
    public void testCreateFromFieldUtility() {
        Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), EXPECTED_FROM_VALUE);

        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), EXPECTED_HOST_VALUE);
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), EXPECTED_PORT_VALUE_STRING);

        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), EXPECTED_AUTH_VALUE_STRING);
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), EXPECTED_USERNAME_VALUE);
        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), EXPECTED_PASSWORD_VALUE);

        addConfigurationFieldToMap(configuredFields, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), EXPECTED_EHLO_VALUE);


        FieldUtility fieldUtility = new FieldUtility(configuredFields);

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();

        Properties properties = javamailPropertiesFactory.createJavaMailProperties(fieldUtility);

        assertEquals(EXPECTED_FROM_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
        assertEquals(EXPECTED_HOST_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        assertEquals(EXPECTED_PORT_VALUE_STRING, properties.getProperty(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        assertEquals(EXPECTED_AUTH_VALUE_STRING, properties.getProperty(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()));
        assertEquals(EXPECTED_USERNAME_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey()));
        assertEquals(EXPECTED_PASSWORD_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()));
        assertEquals(EXPECTED_EHLO_VALUE, properties.getProperty(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey()));
    }

    private void addConfigurationFieldToMap(Map<String, ConfigurationFieldModel> configuredFields, String key, String value) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValue(value);
        configuredFields.put(key, configurationFieldModel);
    }
}
