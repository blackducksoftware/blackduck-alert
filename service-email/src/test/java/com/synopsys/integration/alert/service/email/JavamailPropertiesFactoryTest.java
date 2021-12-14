package com.synopsys.integration.alert.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

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
        emailGlobalConfigModel.setSmtpFrom(EXPECTED_FROM_VALUE);
        emailGlobalConfigModel.setSmtpHost(EXPECTED_HOST_VALUE);
        emailGlobalConfigModel.setSmtpPort(EXPECTED_PORT_VALUE);
        emailGlobalConfigModel.setSmtpAuth(EXPECTED_AUTH_VALUE);
        emailGlobalConfigModel.setSmtpUsername(EXPECTED_USERNAME_VALUE);
        emailGlobalConfigModel.setSmtpPassword(EXPECTED_PASSWORD_VALUE);
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
}
