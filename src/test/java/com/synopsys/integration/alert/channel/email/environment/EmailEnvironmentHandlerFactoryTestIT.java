package com.synopsys.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class EmailEnvironmentHandlerFactoryTestIT {
    public static final String TEST_USER = "testuser";
    public static final String TEST_PORT = "25";
    public static final String TEST_PASSWORD = "a test value";
    public static final String TEST_SMTP_HOST = "test.smtp.server.example.com";
    public static final String TEST_FROM = "noreply@example.com";
    public static final String TEST_AUTH_REQUIRED = "true";

    @Autowired
    private EmailGlobalConfigAccessor emailGlobalConfigAccessor;

    @AfterEach
    public void cleanup() {
        emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(Config::getId)
            .map(UUID::fromString)
            .ifPresent(emailGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void testCleanEnvironment() {
        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(emailGlobalConfigAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(TEST_AUTH_REQUIRED, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY));
        assertEquals(TEST_FROM, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY));
        assertEquals(TEST_SMTP_HOST, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY));
        assertNull(updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY));
        assertEquals(TEST_PORT, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY));
        assertEquals(TEST_USER, updatedProperties.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY));
    }

    @Test
    void testExistingEmailConfig() {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        emailGlobalConfigModel.setSmtpAuth(Boolean.valueOf(TEST_AUTH_REQUIRED));
        emailGlobalConfigModel.setSmtpFrom(TEST_FROM);
        emailGlobalConfigModel.setSmtpHost(TEST_SMTP_HOST);
        emailGlobalConfigModel.setSmtpPassword(TEST_PASSWORD);
        emailGlobalConfigModel.setSmtpPort(Integer.valueOf(TEST_PORT));
        emailGlobalConfigModel.setSmtpUsername(TEST_USER);

        emailGlobalConfigAccessor.createConfiguration(emailGlobalConfigModel);

        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EmailEnvironmentVariableHandlerFactory(emailGlobalConfigAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandlerFactory.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);

        Mockito.doAnswer((invocation -> {
            String environmentVariableName = invocation.getArgument(0);
            return hasEnvVarCheck.test(environmentVariableName);
        })).when(environment).containsProperty(Mockito.anyString());
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY)).thenReturn(TEST_AUTH_REQUIRED);
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY)).thenReturn(TEST_FROM);
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY)).thenReturn(TEST_SMTP_HOST);
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY)).thenReturn(TEST_PASSWORD);
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY)).thenReturn(TEST_PORT);
        Mockito.when(environment.getProperty(EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY)).thenReturn(TEST_USER);
        return environment;
    }
}
