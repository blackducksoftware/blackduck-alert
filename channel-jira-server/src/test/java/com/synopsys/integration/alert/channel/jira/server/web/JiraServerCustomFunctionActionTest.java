package com.synopsys.integration.alert.channel.jira.server.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

class JiraServerCustomFunctionActionTest {
    private final Gson gson = new Gson();
    private AuthenticationTestUtils authenticationTestUtils;
    private PermissionMatrixModel fullPermissions;

    @BeforeEach
    public void initAuthManager() {
        authenticationTestUtils = new AuthenticationTestUtils();
        authenticationTestUtils.addUserWithRole("admin", "ALERT_ADMIN");
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), ChannelKeys.JIRA_SERVER.getUniversalKey());
        fullPermissions = new PermissionMatrixModel(Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS));
    }

    @Test
    void testInstallPlugin() {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(
            new JiraServerGlobalConfigModel(
                UUID.randomUUID().toString(),
                "Jira Server Config",
                "http://jira.server.example.com/jira",
                "user",
                "password"
            )));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any()));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = new JiraServerPropertiesFactory(proxyManager, globalConfigAccessor, jobAccessor);
        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isSuccessful());
    }

    @Test
    void testDefaultConfigMissing() {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.empty());

        JiraServerPropertiesFactory jiraServerPropertiesFactory = new JiraServerPropertiesFactory(proxyManager, globalConfigAccessor, jobAccessor);
        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isSuccessful());
    }

    FieldModel createFieldModel() {
        Map<String, FieldValueModel> fieldValues = Map.of(
            JiraServerDescriptor.KEY_SERVER_URL, new FieldValueModel(List.of("http://jira.server.example.com/jira"), true),
            JiraServerDescriptor.KEY_SERVER_USERNAME, new FieldValueModel(List.of("user"), true),
            JiraServerDescriptor.KEY_SERVER_PASSWORD, new FieldValueModel(List.of("password"), true),
            JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK, new FieldValueModel(List.of("false"), true),
            JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN, new FieldValueModel(List.of("true"), true)
        );
        return new FieldModel(ChannelKeys.JIRA_SERVER.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), fieldValues);
    }
}
