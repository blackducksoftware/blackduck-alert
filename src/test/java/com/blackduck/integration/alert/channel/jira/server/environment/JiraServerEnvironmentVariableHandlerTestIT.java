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

import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.test.common.EnvironmentVariableMockingUtil;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class JiraServerEnvironmentVariableHandlerTestIT {
    public static final String TEST_USER = "testuser";
    public static final String TEST_PASSWORD = "a test value";
    public static final String TEST_URL = "http://test.jira.server.example.com";
    public static final Integer TEST_TIMEOUT = 300;
    public static final String TEST_ACCESS_TOKEN = "JiraServerAccessTokenValue";
    public static final String TEST_DISABLE_PLUGIN_CHECK = "true";

    @Autowired
    private JiraServerGlobalConfigAccessor jiraGlobalConfigAccessor;
    @Autowired
    private JiraServerGlobalConfigurationValidator validator;

    @BeforeEach
    @AfterEach
    public void cleanup() {
        jiraGlobalConfigAccessor.getConfigurationPage(0, 100, null, null, null)
            .getModels()
            .stream()
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString)
            .forEach(jiraGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void testCleanEnvironmentBasicAuth() {
        Environment environment = setupMockedEnvironment(JiraServerAuthorizationMethod.BASIC);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(
            jiraGlobalConfigAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues(), result.toString());

        assertEquals(TEST_DISABLE_PLUGIN_CHECK, result.getVariableValue(JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(TEST_URL, result.getVariableValue(JiraServerEnvironmentVariableHandler.URL_KEY).orElse("Url value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandler.PASSWORD_KEY).orElse("Password value missing"));
        assertEquals(TEST_USER, result.getVariableValue(JiraServerEnvironmentVariableHandler.USERNAME_KEY).orElse("Username value missing"));
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY).isEmpty());
    }

    @Test
    void testCleanEnvironmentBearerAuth() {
        Environment environment = setupMockedEnvironment(JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(
            jiraGlobalConfigAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(TEST_DISABLE_PLUGIN_CHECK, result.getVariableValue(JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY).orElse("Disable plugin check value missing"));
        assertEquals(TEST_URL, result.getVariableValue(JiraServerEnvironmentVariableHandler.URL_KEY).orElse("Url value missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY).orElse("Access Token missing"));
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.PASSWORD_KEY).isEmpty());
        assertTrue(result.getVariableValue(JiraServerEnvironmentVariableHandler.USERNAME_KEY).isEmpty());
    }

    @Test
    void testExistingConfigBasicAuth() throws AlertConfigurationException {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            createdAt,
            TEST_URL,
            TEST_TIMEOUT,
            JiraServerAuthorizationMethod.BASIC,
            TEST_USER,
            TEST_PASSWORD,
            false,
            null,
            false,
            true
        );

        jiraGlobalConfigAccessor.createConfiguration(jiraServerGlobalConfigModel);

        Environment environment = setupMockedEnvironment(JiraServerAuthorizationMethod.BASIC);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(
            jiraGlobalConfigAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    @Test
    void testExistingBearerAuth() throws AlertConfigurationException {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            createdAt,
            TEST_URL,
            TEST_TIMEOUT,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            null,
            null,
            false,
            TEST_ACCESS_TOKEN,
            false,
            true
        );

        jiraGlobalConfigAccessor.createConfiguration(jiraServerGlobalConfigModel);

        Environment environment = setupMockedEnvironment(JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        JiraServerEnvironmentVariableHandler jiraServerEnvironmentVariableHandler = new JiraServerEnvironmentVariableHandler(
            jiraGlobalConfigAccessor,
            environmentVariableUtility,
            validator
        );
        EnvironmentProcessingResult result = jiraServerEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(ChannelKeys.JIRA_SERVER.getDisplayName(), jiraServerEnvironmentVariableHandler.getName());
        assertFalse(result.hasValues());
    }

    private Environment setupMockedEnvironment(JiraServerAuthorizationMethod authorizationMethod) {
        Environment environment = Mockito.mock(Environment.class);
        Predicate<String> hasEnvVarCheck = (variableName) -> !JiraServerEnvironmentVariableHandler.VARIABLE_NAMES.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
            environment,
            hasEnvVarCheck,
            JiraServerEnvironmentVariableHandler.DISABLE_PLUGIN_KEY,
            TEST_DISABLE_PLUGIN_CHECK
        );
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.URL_KEY, TEST_URL);
        if (authorizationMethod.equals(JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN)) {
            EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
                environment,
                hasEnvVarCheck,
                JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY,
                JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN.name()
            );
            EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.ACCESS_TOKEN_KEY, TEST_ACCESS_TOKEN);
        } else {
            EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(
                environment,
                hasEnvVarCheck,
                JiraServerEnvironmentVariableHandler.AUTHORIZATION_METHOD_KEY,
                JiraServerAuthorizationMethod.BASIC.name()
            );
            EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.PASSWORD_KEY, TEST_PASSWORD);
            EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, JiraServerEnvironmentVariableHandler.USERNAME_KEY, TEST_USER);
        }

        return environment;
    }
}

