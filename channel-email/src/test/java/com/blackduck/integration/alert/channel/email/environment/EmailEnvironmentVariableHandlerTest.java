package com.blackduck.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.test.common.EnvironmentVariableMockingUtil;

@ExtendWith(SpringExtension.class)
class EmailEnvironmentVariableHandlerTest {
    private final EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();

    public static final String ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_EHLO";
    private static final String AUTH_REQUIRED = "true";
    private static final String FROM = "noreply@example.com";
    private static final String SMTP_HOST = "test.smtp.server.example.com";
    private static final String PORT = "25";
    private static final String USERNAME = "testuser";
    private static final String INCORRECT_SMTP_PROPERTY_NAME = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_DNS_RET";
    private static final String CORRECT_SMTP_PROPERTY_NAME = "ALERT_CHANNEL_EMAIL_MAIL_SMTP_DSN_RET";
    private static final String EXPECTED_SMTP_VALUE = "expected-value";

    @Mock
    Environment mockEnvironment;
    @Mock
    EmailGlobalConfigAccessor MockConfigAccessor;

    @BeforeEach
    void initEach() {
        Mockito.when(MockConfigAccessor.doesConfigurationExist()).thenReturn(false);
        Mockito.when(mockEnvironment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
    }

    @Test
    void testEmailAdditionalPropertyNameConversion() {
        assertEquals("mail.smtp.ehlo", EmailEnvironmentVariableHandler.convertVariableNameToJavamailPropertyKey(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME));
    }

    @Test
    void testEmailSetInEnvironment() {
        Set<String> expectedVariableNames = Stream.concat(
                EmailEnvironmentVariableHandler.EMAIL_CONFIGURATION_KEYSET.stream(),
                EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.stream()
            )
            .collect(Collectors.toSet());
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, emailEnvironmentVariableHandler.getVariableNames());
    }

    @Test
    void testEmailMissingFromEnvironment() {
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testEmailConfigPresent() {
        Mockito.when(MockConfigAccessor.doesConfigurationExist()).thenReturn(true);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testEmailAdditionalProperties() {
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
            || ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME.equals(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(mockEnvironment, hasEnvVarCheck, ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME, "true");

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertEquals("true", result.getVariableValue(ADDITIONAL_EMAIL_PROPERTY_VARIABLE_NAME).orElse("Additional email property value missing"));
    }

    @Test
    void testSmtpDsnRetOnlyCorrectSet() {
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
            || CORRECT_SMTP_PROPERTY_NAME.equals(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(mockEnvironment, hasEnvVarCheck, CORRECT_SMTP_PROPERTY_NAME, EXPECTED_SMTP_VALUE);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertEquals(EXPECTED_SMTP_VALUE, result.getVariableValue(CORRECT_SMTP_PROPERTY_NAME).orElse("Property value missing for " + CORRECT_SMTP_PROPERTY_NAME));
        assertTrue(result.getVariableValue(INCORRECT_SMTP_PROPERTY_NAME).isEmpty());
    }

    @Test
    void testSmtpDsnRetOnlyIncorrectSet() {
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
            || INCORRECT_SMTP_PROPERTY_NAME.equals(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(mockEnvironment, hasEnvVarCheck, INCORRECT_SMTP_PROPERTY_NAME, EXPECTED_SMTP_VALUE);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertEquals(EXPECTED_SMTP_VALUE, result.getVariableValue(CORRECT_SMTP_PROPERTY_NAME).orElse("Property value missing for " + CORRECT_SMTP_PROPERTY_NAME));
        assertTrue(result.getVariableValue(INCORRECT_SMTP_PROPERTY_NAME).isEmpty());
    }

    @Test
    void testSmtpDsnRetBothSet() {
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName)
            || CORRECT_SMTP_PROPERTY_NAME.equals(variableName) || INCORRECT_SMTP_PROPERTY_NAME.equals(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(mockEnvironment, hasEnvVarCheck, CORRECT_SMTP_PROPERTY_NAME, EXPECTED_SMTP_VALUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(mockEnvironment, hasEnvVarCheck, INCORRECT_SMTP_PROPERTY_NAME, "Should not see this");

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertEquals(EXPECTED_SMTP_VALUE, result.getVariableValue(CORRECT_SMTP_PROPERTY_NAME).orElse("Property value missing for " + CORRECT_SMTP_PROPERTY_NAME));
        assertTrue(result.getVariableValue(INCORRECT_SMTP_PROPERTY_NAME).isEmpty());
    }

    @Test
    void testSmtpDsnRetNeitherSet() {
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);

        addDefaultEnvironmentVariables(mockEnvironment, hasEnvVarCheck);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(MockConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();

        validateDefaultEnvironmentVariables(result);
        assertFalse(result.getVariableValue(CORRECT_SMTP_PROPERTY_NAME).isEmpty());
        assertTrue(result.getVariableValue(INCORRECT_SMTP_PROPERTY_NAME).isEmpty());
    }

    private void addDefaultEnvironmentVariables(Environment environment, Predicate<String> hasEnvVarCheck) {
        String PASSWORD_VALUE = "a test value";
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY, AUTH_REQUIRED);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_FROM_KEY, FROM);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_HOST_KEY, SMTP_HOST);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY, PASSWORD_VALUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_PORT_KEY, PORT);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_USER_KEY, USERNAME);
    }

    private void validateDefaultEnvironmentVariables(EnvironmentProcessingResult result) {
        assertTrue(result.hasValues());
        assertEquals(AUTH_REQUIRED, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY).orElse("Auth required value missing"));
        assertEquals(FROM, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_FROM_KEY).orElse("SMTP from value missing"));
        assertEquals(SMTP_HOST, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_HOST_KEY).orElse("SMTP host value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY).orElse("Auth password value missing"));
        assertEquals(PORT, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_PORT_KEY).orElse("SMTP port value missing"));
        assertEquals(USERNAME, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_USER_KEY).orElse("Auth user value missing"));
    }

}
