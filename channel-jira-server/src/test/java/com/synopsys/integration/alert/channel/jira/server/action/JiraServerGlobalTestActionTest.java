package com.synopsys.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerConfigurationRepository;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import com.synopsys.integration.exception.IntegrationException;

class JiraServerGlobalTestActionTest {
    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;
    private JiraServerGlobalConfigurationValidator jiraServerGlobalConfigurationValidator;
    private final JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicAuthConfig();

    @BeforeEach
    void initEach() {
        MockRepositorySorter<JiraServerConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockJiraServerConfigurationRepository mockJiraServerConfigurationRepository = new MockJiraServerConfigurationRepository(sorter);
        jiraServerGlobalConfigAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, mockJiraServerConfigurationRepository);
        jiraServerGlobalConfigurationValidator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
    }

    @Test
    void testConfigValidTest() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testConfigValidWithPasswordSavedTest() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(false);

        JiraServerGlobalConfigModel configurationModel = new JiraServerGlobalConfigModel(
            jiraServerGlobalConfigModel.getId(),
            jiraServerGlobalConfigModel.getName(),
            jiraServerGlobalConfigModel.getCreatedAt(),
            jiraServerGlobalConfigModel.getLastUpdated(),
            jiraServerGlobalConfigModel.getUrl(),
            JiraServerAuthorizationMethod.BASIC,
            jiraServerGlobalConfigModel.getUserName().orElse(null),
            null,
            Boolean.TRUE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(configurationModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testConfigValidWithAccessTokenSavedTest() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(false);

        JiraServerGlobalConfigModel configurationModel = new JiraServerGlobalConfigModel(
            jiraServerGlobalConfigModel.getId(),
            jiraServerGlobalConfigModel.getName(),
            jiraServerGlobalConfigModel.getCreatedAt(),
            jiraServerGlobalConfigModel.getLastUpdated(),
            jiraServerGlobalConfigModel.getUrl(),
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            null,
            null,
            Boolean.FALSE,
            null,
            Boolean.TRUE,
            Boolean.FALSE
        );

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(configurationModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testConfigIssueTrackerException() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenThrow(new IssueTrackerException("Test Exception message"));

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicAuthConfig();

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testUserCannotGetIssues() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testAppCheckDisabled() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(false);
        Mockito.when(testActionWrapper.isUserAdmin()).thenThrow(new IntegrationException("Test failure: This exception should not be thrown!"));

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertTrue(testResult.isSuccess());
    }

    @Test
    void testUserAdminMissing() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(false);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testAppMissing() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(true);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ConfigurationTestResult testResult = jiraServerGlobalTestAction.testConfigModelContent(jiraServerGlobalConfigModel);
        assertFalse(testResult.isSuccess());
    }

    @Test
    void testPermissionForbidden() {
        AuthorizationManager authorizationManager = createAuthorizationManager(0);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
        ActionResponse<ValidationResponseModel> validationResponseModel = jiraServerGlobalTestAction.testWithPermissionCheck(jiraServerGlobalConfigModel);
        assertTrue(validationResponseModel.isError());
        assertEquals(HttpStatus.FORBIDDEN, validationResponseModel.getHttpStatus());
    }

    @Test
    void testPermissionOK() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(255);
        JiraServerTestActionFactory jiraServerTestActionFactory = Mockito.mock(JiraServerTestActionFactory.class);
        JiraServerGlobalTestActionWrapper testActionWrapper = Mockito.mock(JiraServerGlobalTestActionWrapper.class);

        Mockito.when(jiraServerTestActionFactory.createTestActionWrapper(Mockito.any())).thenReturn(testActionWrapper);
        Mockito.when(testActionWrapper.canUserGetIssues()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppCheckEnabled()).thenReturn(true);
        Mockito.when(testActionWrapper.isUserAdmin()).thenReturn(true);
        Mockito.when(testActionWrapper.isAppMissing()).thenReturn(true);

        JiraServerGlobalTestAction jiraServerGlobalTestAction = new JiraServerGlobalTestAction(
            authorizationManager,
            jiraServerGlobalConfigurationValidator,
            jiraServerTestActionFactory,
            jiraServerGlobalConfigAccessor
        );
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

    private JiraServerGlobalConfigModel createBasicAuthConfig() {
        return new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            "jiraServerConfigName",
            "createdAtTest",
            "lastUpdatedTest",
            "https://jiraServer",
            JiraServerAuthorizationMethod.BASIC,
            "jiraUser",
            "jiraPassword",
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );
    }
}
