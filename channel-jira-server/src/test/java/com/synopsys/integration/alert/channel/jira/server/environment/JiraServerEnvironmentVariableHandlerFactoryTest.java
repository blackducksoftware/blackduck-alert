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
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.test.common.EnvironmentVariableMockingUtil;

class JiraServerEnvironmentVariableHandlerFactoryTest {
    @Test
    void testSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "test.jira.server.example.com";
        String passwordValue = "a test value";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(disablePluginCheck, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(url, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.URL_KEY).orElse("Url value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY).orElse("Password value missing"));
        assertEquals(username, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY).orElse("Username value missing"));
    }

    @Test
    void testSetInEnvironmentURLMissing() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String passwordValue = "a test value";
        String username = "testuser";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(result.hasValues());
    }

    @Test
    void testSetInEnvironmentUserNameMissing() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "test.jira.server.example.com";
        String passwordValue = "a test value";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandlerFactory.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY, passwordValue);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(disablePluginCheck, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(url, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.URL_KEY).orElse("Url value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY).orElse("Password value missing"));
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY).isEmpty());
    }

    @Test
    void testMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testConfigPresent() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(1L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertFalse(result.hasValues());
    }
}
