package com.synopsys.integration.alert.channel.jira.server.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;

class JiraServerEnvironmentVariableHandlerTest {
    private final JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();

    @Test
    void testSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "http://test.jira.server.example.com";
        String passwordValue = "a test value";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.USERNAME_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(disablePluginCheck, result.getVariableValue(JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(url, result.getVariableValue(JiraServerEnvironmentVariableHandler.URL_KEY).orElse("Url value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandler.PASSWORD_KEY).orElse("Password value missing"));
        assertEquals(username, result.getVariableValue(JiraServerEnvironmentVariableHandler.USERNAME_KEY).orElse("Username value missing"));
    }

    @Test
    void testSetInEnvironmentURLMissing() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String passwordValue = "a test value";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.USERNAME_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertFalse(result.hasValues());
    }

    @Test
    void testSetInEnvironmentUserNameMissing() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "http://test.jira.server.example.com";
        String passwordValue = "a test value";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertFalse(result.hasValues());
    }

    @Test
    void testMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testConfigPresent() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(1L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }
}
