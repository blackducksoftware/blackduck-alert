package com.synopsys.integration.alert.channel.jira.server.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.rest.exception.IntegrationRestException;

class JiraServerCustomFunctionActionTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
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
    void testInstallPluginReadIDFromDB() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any())).thenReturn(HttpStatus.OK.value());
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.any())).thenReturn(Boolean.TRUE);

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
    void testInstallPluginReadIdFromModel() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any())).thenReturn(HttpStatus.OK.value());
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.any())).thenReturn(Boolean.TRUE);

        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId(jiraGlobalConfigId.toString());
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isSuccessful());
    }

    @Test
    void testInstallPluginReadIdFromModelInvalid() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any())).thenReturn(HttpStatus.OK.value());
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.any())).thenReturn(Boolean.TRUE);

        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("Unparseable id");
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isSuccessful());
    }

    @Test
    void testInstallPluginThrowsNotFoundException() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any()))
            .thenThrow(new IntegrationRestException(null, null, HttpStatus.NOT_FOUND.value(), "", "", ""));

        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId(jiraGlobalConfigId.toString());
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isError());
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    }

    @Test
    void testInstallPluginThrowsRestException() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any()))
            .thenThrow(new IntegrationRestException(null, null, HttpStatus.BAD_REQUEST.value(), "", "", ""));

        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId(jiraGlobalConfigId.toString());
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isError());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    void testInstallPluginAppNotInstalled() throws Exception {
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "ALERT_ADMIN", () -> fullPermissions);
        HttpServletContentWrapper contentWrapper = Mockito.mock(HttpServletContentWrapper.class);
        JiraServerGlobalConfigurationFieldModelValidator jiraServerGlobalConfigurationFieldModelValidator = Mockito.mock(JiraServerGlobalConfigurationFieldModelValidator.class);
        JiraServerGlobalConfigAccessor globalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        UUID jiraGlobalConfigId = UUID.randomUUID();
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            jiraGlobalConfigId.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "http://jira.server.example.com/jira",
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName("user");
        configModel.setPassword("password");
        DistributionJobModel jobModel = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .enabled(true)
            .name("A Job")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.JIRA_SERVER.getUniversalKey())
            .blackDuckGlobalConfigId(1L)
            .channelGlobalConfigId(jiraGlobalConfigId)
            .createdAt(OffsetDateTime.now())
            .notificationTypes(List.of("irrelevant_string"))
            .build();

        Mockito.when(globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(configModel));
        Mockito.when(globalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(configModel));
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModel));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        PluginManagerService pluginManagerService = Mockito.mock(PluginManagerService.class);
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(jiraGlobalConfigId)).thenReturn(jiraProperties);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(serviceFactory);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(pluginManagerService.installMarketplaceServerApp(Mockito.any())).thenReturn(HttpStatus.OK.value());
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.any())).thenReturn(Boolean.FALSE);

        JiraServerCustomFunctionAction action = new JiraServerCustomFunctionAction(
            authorizationManager,
            jiraServerGlobalConfigurationFieldModelValidator,
            jiraServerPropertiesFactory,
            gson,
            globalConfigAccessor
        );
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId(jiraGlobalConfigId.toString());
        ActionResponse<String> response = action.createActionResponse(fieldModel, contentWrapper);

        assertTrue(response.isError());
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
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

        assertTrue(response.isError());
        assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
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
