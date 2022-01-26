package com.synopsys.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;
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

    @BeforeEach
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
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertTrue(result.hasValues());

        assertEquals(TEST_AUTH_REQUIRED, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY).orElse(null));
        assertEquals(TEST_FROM, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY).orElse(null));
        assertEquals(TEST_SMTP_HOST, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY).orElse(null));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY).orElse(null));
        assertEquals(TEST_PORT, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY).orElse(null));
        assertEquals(TEST_USER, result.getVariableValue(EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY).orElse(null));
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
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), handler.getName());
        assertFalse(result.hasValues());
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandlerFactory.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_REQUIRED_KEY, TEST_AUTH_REQUIRED);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_FROM_KEY, TEST_FROM);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_HOST_KEY, TEST_SMTP_HOST);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_PASSWORD_KEY, TEST_PASSWORD);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.EMAIL_PORT_KEY, TEST_PORT);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandlerFactory.AUTH_USER_KEY, TEST_USER);
        return environment;
    }
}
