package com.synopsys.integration.alert.channel.jira.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.environment.JiraServerEnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class JiraServerEnvironmentHandlerFactoryTestIT {
    public static final String TEST_USER = "testuser";
    public static final String TEST_PASSWORD = "a test value";
    public static final String TEST_URL = "test.jira.server.example.com";
    public static final String TEST_DISABLE_PLUGIN_CHECK = "true";

    @Autowired
    private JiraServerGlobalConfigAccessor jiraGlobalConfigAccessor;

    @BeforeEach
    @AfterEach
    public void cleanup() {
        jiraGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(Config::getId)
            .map(UUID::fromString)
            .ifPresent(jiraGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void testCleanEnvironment() {
        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(jiraGlobalConfigAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(TEST_DISABLE_PLUGIN_CHECK, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY));
        assertEquals(TEST_URL, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.URL_KEY));
        assertEquals(AlertRestConstants.MASKED_VALUE, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY));
        assertEquals(TEST_USER, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY));
    }

    @Test
    void testExistingConfig() {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        JiraServerGlobalConfigModel emailGlobalConfigModel = new JiraServerGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, createdAt, createdAt, TEST_URL, TEST_USER, TEST_PASSWORD, false, true);

        jiraGlobalConfigAccessor.createConfiguration(emailGlobalConfigModel);

        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(jiraGlobalConfigAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY, TEST_DISABLE_PLUGIN_CHECK);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.URL_KEY, TEST_URL);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY, TEST_PASSWORD);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY, TEST_USER);
        return environment;
    }
}
