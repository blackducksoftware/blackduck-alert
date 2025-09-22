/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.test.common.EnvironmentVariableMockingUtil;

class JiraServerEnvironmentVariableHandlerTest {
    private static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;

    @Test
    void testSetInEnvironmentBasicAuth() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "http://test.jira.server.example.com";
        String timeout = String.valueOf(TEST_JIRA_TIMEOUT_SECONDS);
        String passwordValue = "a test value";
        String username = "testuser";
        String authorizationMethod = JiraServerAuthorizationMethod.BASIC.name();
        Predicate<String> hasEnvVarCheck = variableName -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.USERNAME_KEY, username);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
            environment,
            hasEnvVarCheck,
            JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY,
            authorizationMethod
        );

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(disablePluginCheck, result.getVariableValue(JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(url, result.getVariableValue(JiraServerEnvironmentVariableHandler.URL_KEY).orElse("Url value missing"));
        assertEquals(timeout, result.getVariableValue(JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY).orElse("Timeout value missing"));
        assertEquals(authorizationMethod, result.getVariableValue(JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY).orElse("AuthorizationMethod is missing."));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandler.PASSWORD_KEY).orElse("Password value missing"));
        assertEquals(username, result.getVariableValue(JiraServerEnvironmentVariableHandler.USERNAME_KEY).orElse("Username value missing"));
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY).isEmpty());
    }

    @Test
    void testSetInEnvironmentPersonalAccessTokenAuth() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;

        String disablePluginCheck = "true";
        String url = "http://test.jira.server.example.com";
        String timeout = String.valueOf(TEST_JIRA_TIMEOUT_SECONDS);
        String authorizationMethod = JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN.name();
        String accessToken = "testPersonalAccessToken";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
            environment,
            hasEnvVarCheck,
            JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY,
            authorizationMethod
        );
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY, accessToken);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertTrue(result.hasValues());

        assertEquals(disablePluginCheck, result.getVariableValue(JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(url, result.getVariableValue(JiraServerEnvironmentVariableHandler.URL_KEY).orElse("Url value missing"));
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        assertEquals(authorizationMethod, result.getVariableValue(JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY).orElse("AuthorizationMethod is missing."));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY).orElse("Access token value missing"));
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.USERNAME_KEY).isEmpty());
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.PASSWORD_KEY).isEmpty());
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
        String timeout = String.valueOf(TEST_JIRA_TIMEOUT_SECONDS);
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.USERNAME_KEY, username);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
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
        String timeout = String.valueOf(TEST_JIRA_TIMEOUT_SECONDS);
        String passwordValue = "a test value";
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, passwordValue);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
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
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
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
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }
    
    @Test
    void testWrongAuthorizationMethodValuesSet() {
        Environment environment = Mockito.mock(Environment.class);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationCount()).thenReturn(0L);
        Set<String> expectedVariableNames = JiraServerEnvironmentVariableHandler.VARIABLE_NAMES;
        String url = "http://test.jira.server.example.com";
        String timeout = String.valueOf(TEST_JIRA_TIMEOUT_SECONDS);
        String accessToken = "testPersonalAccessToken";
        String disablePluginCheck = "true";
        String authorizationMethod = JiraServerAuthorizationMethod.BASIC.name();

        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);

        Mockito.when(environment.containsProperty(Mockito.anyString())).thenReturn(Boolean.TRUE);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY, disablePluginCheck);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, url);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.JIRA_TIMEOUT_KEY, timeout);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
            environment,
            hasEnvVarCheck,
            JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY,
            authorizationMethod
        );
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY, accessToken);

        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(configAccessor, environmentVariableUtility, validator);
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, jiraServerEnvironmentVariableHandler.getVariableNames());
        assertFalse(result.hasValues());
    }
}
