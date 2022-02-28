package com.synopsys.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.exception.IntegrationException;

class JiraServerGlobalTestActionTest {
    private final JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createValidJiraServerGlobalConfigModel();

    @Test
    void testConfigValidTest() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testConfigIssueTrackerException() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenThrow(new IssueTrackerException("Test Exception message"));

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createValidJiraServerGlobalConfigModel();

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testUserCannotGetIssues() throws IntegrationException  {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testAppCheckDisabled() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(false);
        Mockito.when(testActionWrapper.isUserAdmin()).thenThrow(new IntegrationException("Test failure: This exception should not be thrown!"));

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testUserAdminMissing() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testAppMissing() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(true);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testPermissionForbidden() {
        AuthorizationManager authorizationManager = createAuthorizationManager(0);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ActionResponse<ValidationResponseModel> validationResponseModel = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);
        assertTrue(validationResponseModel.isError());
        assertEquals(HttpStatus.FORBIDDEN, validationResponseModel.getHttpStatus());
    }

    @Test
    void testPermissionOK() throws IntegrationException{
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(true);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(authorizationManager, validator, jiraServerTestActionFactory);
        ActionResponse<ValidationResponseModel> validationResponseModel = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);
        assertTrue(validationResponseModel.isSuccessful());
        assertEquals(HttpStatus.OK, validationResponseModel.getHttpStatus());
    }


    private AuthorizationManager createAuthorizationManager(int assignedPermissions) {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, assignedPermissions);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private JiraServerGlobalConfigModel createValidJiraServerGlobalConfigModel() {
        return new JiraServerGlobalConfigModel(UUID.randomUUID().toString(), "jiraServerConfigName", "createdAtTest", "lastUpdatedTest", "https://jiraServer", "jiraUser", "jiraPassword", Boolean.FALSE, Boolean.FALSE);
    }
}
