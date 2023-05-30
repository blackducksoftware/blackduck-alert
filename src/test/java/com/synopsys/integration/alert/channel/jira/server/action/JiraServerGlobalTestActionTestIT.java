package com.synopsys.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

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
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName(testJiraServerUsername);
        jiraServerGlobalConfigModel.setPassword(testJiraServerPassword);
        return jiraServerGlobalConfigModel;
    }
}
