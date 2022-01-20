package com.synopsys.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

public class EmailEnvironmentHandlerFactoryTest {

    public static final String ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_EHLO";

    @Test
    public void testEmailAdditionalPropertyNameConversion() {
        assertEquals("mail.smtp.ehlo", EmailEnvironmentVariableHandlerFactory.convertVariableNameToJavamailPropertyKey(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME));
    }

    @Test
    public void testEmailSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = Stream.concat(EmailEnvironmentVariableHandlerFactory.EMAIL_CONFIGURATION_KEYSET.stream(), EmailEnvironmentVariableHandlerFactory.OLD_ADDITIONAL_PROPERTY_KEYSET.stream())
            .collect(Collectors.toSet());

        String authRequired = "true";
        String from = "noreply@example.com";
        String smtpHost = "test.smtp.server.example.com";
        String passwordValue = "a test value";
        String port = "25";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandlerFactory.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY, authRequired);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY, from);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY, smtpHost);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY, passwordValue);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY, port);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(authRequired, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY));
        assertEquals(from, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY));
        assertEquals(smtpHost, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY));
        assertNull(updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY));
        assertEquals(port, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY));
        assertEquals(username, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY));
    }

    @Test
    public void testEmailMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }

    @Test
    public void testEmailConfigPresent() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(1L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }

    @Test
    public void testEmailAdditionalProperties() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);

        String authRequired = "true";
        String from = "noreply@example.com";
        String smtpHost = "test.smtp.server.example.com";
        String passwordValue = "a test value";
        String port = "25";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandlerFactory.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
                                                                 || ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME.equals(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY, authRequired);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY, from);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY, smtpHost);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY, passwordValue);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY, port);
        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY, username);

        setupConfigurationEnvironmentVariable(environment, hasEnvVarCheck, ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME, "true");

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(authRequired, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY));
        assertEquals(from, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY));
        assertEquals(smtpHost, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY));
        assertNull(updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY));
        assertEquals(port, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY));
        assertEquals(username, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY));

        assertEquals("true", updatedProperties.getProperty(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME));
    }

    private void setupConfigurationEnvironmentVariable(Environment mockedEnvironment, Predicate<String> hasEnvVarCheck, String propertyKey, String value) {
        Mockito.doAnswer((invocation -> {
            String environmentVariableName = invocation.getArgument(0);
            return hasEnvVarCheck.test(environmentVariableName);
        })).when(mockedEnvironment).containsProperty(Mockito.anyString());
        Mockito.when(mockedEnvironment.getProperty(propertyKey)).thenReturn(value);
    }
}
