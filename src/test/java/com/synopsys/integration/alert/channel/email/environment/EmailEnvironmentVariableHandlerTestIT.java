package com.synopsys.integration.alert.channel.email.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.email.environment.EmailEnvironmentVariableHandler;
import com.blackduck.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class EmailEnvironmentVariableHandlerTestIT {
    public static final String TEST_USER = "testuser";
    public static final String TEST_PORT = "25";
    public static final String TEST_PASSWORD = "a test value";
    public static final String TEST_SMTP_HOST = "test.smtp.server.example.com";
    public static final String TEST_FROM = "noreply@example.com";
    public static final String TEST_AUTH_REQUIRED = "true";

    @Autowired
    private EmailGlobalConfigAccessor emailGlobalConfigAccessor;
    @Autowired
    private EmailGlobalConfigurationValidator validator;

    @AfterEach
    @BeforeEach
    public void cleanup() {
        if (emailGlobalConfigAccessor.doesConfigurationExist()) {
            emailGlobalConfigAccessor.deleteConfiguration();
        }
    }

    @Test
    void testCleanEnvironment() {
        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(emailGlobalConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(TEST_AUTH_REQUIRED, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY).orElse("Auth required value missing"));
        assertEquals(TEST_FROM, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_FROM_KEY).orElse("SMTP from value missing"));
        assertEquals(TEST_SMTP_HOST, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_HOST_KEY).orElse("SMTP host value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY).orElse("Auth password value missing"));
        assertEquals(TEST_PORT, result.getVariableValue(EmailEnvironmentVariableHandler.EMAIL_PORT_KEY).orElse("SMTP port value missing"));
        assertEquals(TEST_USER, result.getVariableValue(EmailEnvironmentVariableHandler.AUTH_USER_KEY).orElse("Auth user value missing"));
    }

    @Test
    void testExistingEmailConfig() throws AlertConfigurationException {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, TEST_FROM, TEST_SMTP_HOST);
        emailGlobalConfigModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        emailGlobalConfigModel.setSmtpAuth(Boolean.valueOf(TEST_AUTH_REQUIRED));
        emailGlobalConfigModel.setSmtpPassword(TEST_PASSWORD);
        emailGlobalConfigModel.setSmtpPort(Integer.valueOf(TEST_PORT));
        emailGlobalConfigModel.setSmtpUsername(TEST_USER);

        emailGlobalConfigAccessor.createConfiguration(emailGlobalConfigModel);

        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EmailEnvironmentVariableHandler emailEnvironmentVariableHandler = new EmailEnvironmentVariableHandler(emailGlobalConfigAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = emailEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.EMAIL.getDisplayName(), emailEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !EmailEnvironmentVariableHandler.OLD_ADDITIONAL_PROPERTY_KEYSET.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_REQUIRED_KEY, TEST_AUTH_REQUIRED);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_FROM_KEY, TEST_FROM);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_HOST_KEY, TEST_SMTP_HOST);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_PASSWORD_KEY, TEST_PASSWORD);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.EMAIL_PORT_KEY, TEST_PORT);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EmailEnvironmentVariableHandler.AUTH_USER_KEY, TEST_USER);
        return environment;
    }
}
