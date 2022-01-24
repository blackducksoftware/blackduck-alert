package com.synopsys.integration.alert.channel.jira.server.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
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
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(disablePluginCheck, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY));
        assertEquals(url, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.URL_KEY));
        assertNull(updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY));
        assertEquals(username, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY));
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
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertTrue(updatedProperties.isEmpty());
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
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(updatedProperties.isEmpty());

        assertEquals(disablePluginCheck, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.DISABLE_PLUGIN_KEY));
        assertEquals(url, updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.URL_KEY));
        assertNull(updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.PASSWORD_KEY));
        assertNull(updatedProperties.getProperty(JiraServerEnvironmentVariableHandlerFactory.USERNAME_KEY));
    }

    @Test
    void testMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }

    @Test
    void testConfigPresent() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(1L);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new JiraServerEnvironmentVariableHandlerFactory(configAccessor, environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), handler.getName());
        assertTrue(updatedProperties.isEmpty());
    }
}
