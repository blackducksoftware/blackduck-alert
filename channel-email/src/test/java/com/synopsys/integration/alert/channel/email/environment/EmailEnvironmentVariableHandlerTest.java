package com.synopsys.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;

class EmailEnvironmentVariableHandlerTest {
    private final EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
    public static final String ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_EHLO";

    @Test
    void testEmailAdditionalPropertyNameConversion() {
        assertEquals("mail.smtp.ehlo", EmailEnvironmentVariableHandler.convertVariableNameToJavamailPropertyKey(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME));
    }

    @Test
    void testEmailSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.doesConfigurationExist()).thenReturn(false);
        Set<String> expectedVariableNames = Stream.concat(EmailEnvironmentVariableHandler.EMAIL_CONFIGURATION_KEYSET.stream(), EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.stream())
            .collect(Collectors.toSet());

        String authRequired = "true";
        String from = "noreply@example.com";
        String smtpHost = "test.smtp.server.example.com";
        String passwordValue = "a test value";
        String port = "25";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY, authRequired);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_FROM_KEY, from);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_HOST_KEY, smtpHost);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_PORT_KEY, port);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_USER_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, emailEnvironmentVariableHandler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(authRequired, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY).orElse("Auth required value missing"));
        assertEquals(from, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_FROM_KEY).orElse("SMTP from value missing"));
        assertEquals(smtpHost, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_HOST_KEY).orElse("SMTP host value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY).orElse("Auth password value missing"));
        assertEquals(port, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_PORT_KEY).orElse("SMTP port value missing"));
        assertEquals(username, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_USER_KEY).orElse("Auth user value missing"));
    }

    @Test
    void testEmailMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.doesConfigurationExist()).thenReturn(false);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testEmailConfigPresent() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.doesConfigurationExist()).thenReturn(true);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testEmailAdditionalProperties() {
        Environment environment = Mockito.mock(Environment.class);
        EmailGlobalConfigAccessor configAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(configAccessor.doesConfigurationExist()).thenReturn(false);

        String authRequired = "true";
        String from = "noreply@example.com";
        String smtpHost = "test.smtp.server.example.com";
        String passwordValue = "a test value";
        String port = "25";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
            || ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME.equals(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY, authRequired);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_FROM_KEY, from);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_HOST_KEY, smtpHost);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_PORT_KEY, port);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_USER_KEY, username);

        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME, "true");

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(authRequired, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY).orElse("Auth required value missing"));
        assertEquals(from, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_FROM_KEY).orElse("SMTP from value missing"));
        assertEquals(smtpHost, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_HOST_KEY).orElse("SMTP host value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY).orElse("Auth password value missing"));
        assertEquals(port, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_PORT_KEY).orElse("SMTP port value missing"));
        assertEquals(username, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_USER_KEY).orElse("Auth user value missing"));

        assertEquals("true", result.getVariableValue(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME).orElse("Additional email property value missing"));
    }

}
