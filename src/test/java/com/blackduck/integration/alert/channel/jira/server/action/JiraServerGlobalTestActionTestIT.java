/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class JiraServerGlobalTestActionTestIT {
    private final TestProperties testProperties = new TestProperties();
    private AuthorizationManager authorizationManager;
    @Autowired
    private JiraServerGlobalConfigurationValidator jiraServerGlobalConfigurationValidator;
    @Autowired
    private JiraServerTestActionFactory jiraServerTestActionFactory;
    @Autowired
    private JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    private final String testJiraServerUrl = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
    private final String testJiraServerUsername = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
    private final String testJiraServerPassword = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);
    private final Integer jiraTimeoutSeconds =  Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_TIMEOUT));

    @BeforeEach
    void init() {
        cleanup();
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    @AfterEach
    void cleanup() {
        jiraServerGlobalConfigAccessor.getConfigurationPage(0, 100, null, null, null)
            .getModels()
            .stream()
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString)
            .forEach(jiraServerGlobalConfigAccessor::deleteConfiguration);
    }

    @Test
    void testBasicAuthConfig() {
        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicConfigModel();
        ActionResponse<ValidationResponseModel> response = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);

        ValidationResponseModel validationResponseModel = response.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModel.hasErrors(), "Error occurred when none expected: " + validationResponseModel.getMessage());
    }

    @Test
    void testPasswordFailure() {
        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            "testJiraGlobalConfig",
            testJiraServerUrl,
            jiraTimeoutSeconds,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName(testJiraServerUsername);
        jiraServerGlobalConfigModel.setPassword("Invalid Password");
        ActionResponse<ValidationResponseModel> response = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);

        ValidationResponseModel validationResponseModel = response.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertTrue(validationResponseModel.hasErrors(), "No errors occurred when an error was expected: " + validationResponseModel.getMessage());
    }

    @Test
    void testPasswordSavedToDatabase() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerGlobalConfigAccessor.createConfiguration(createBasicConfigModel());
        jiraServerGlobalConfigModel.setPassword(null);
        jiraServerGlobalConfigModel.setName("newTestName");
        assertTrue(jiraServerGlobalConfigModel.getPassword().isEmpty());
        assertTrue(jiraServerGlobalConfigModel.getIsPasswordSet().orElse(Boolean.FALSE));

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ActionResponse<ValidationResponseModel> response = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);
        ValidationResponseModel validationResponseModel = response.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModel.hasErrors(), "Error occurred when none expected: " + validationResponseModel.getMessage());
    }

    private JiraServerGlobalConfigModel createBasicConfigModel() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            "testJiraGlobalConfig",
            testJiraServerUrl,
            jiraTimeoutSeconds,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName(testJiraServerUsername);
        jiraServerGlobalConfigModel.setPassword(testJiraServerPassword);
        return jiraServerGlobalConfigModel;
    }
}
