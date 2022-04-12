package com.synopsys.integration.alert.performance;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.web.JiraServerCustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerV2;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerV2;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class JiraServerPerformanceTestV2 {
    private static final String PERFORMANCE_JOB_NAME = "Jira Server Performance Job";
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final BlackDuckProviderKey PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JiraServerCustomFunctionAction jiraServerCustomFunctionAction;

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    @Test
    @Disabled
    void jiraServerJobTest() throws Exception {
        TestProperties testProperties = new TestProperties();
        FieldModel globalConfig = createGlobalConfig(testProperties);
        //TODO: Replace the fieldModel with the concrete model
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createGlobalConfigModel(testProperties);

        // Install plugin
        //TODO: Call new install endpoint
        /*ActionResponse<String> actionResponse = jiraServerCustomFunctionAction.createActionResponse(globalConfig, null);
        if (actionResponse.isError()) {
            fail("Unable to install the Alert plugin for Jira Server. Exiting test...");
        }*/

        Map<String, FieldValueModel> channelFieldsMap = createChannelFieldsMap(testProperties);

        IntegrationPerformanceTestRunnerV2 testRunner = createTestRunner();
        testRunner.runTest(jiraServerGlobalConfigModel, JiraServerGlobalConfigModel.class, channelFieldsMap, PERFORMANCE_JOB_NAME);
    }

    private JiraServerGlobalConfigModel createGlobalConfigModel(TestProperties testProperties) {
        UUID uuid = UUID.randomUUID();
        String createdAt = OffsetDateTime.now().toString();
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
        String userName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
        String password = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);
        Boolean disablePluginCheck = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK)
            .map(Boolean::valueOf)
            .orElse(Boolean.FALSE);

        return new JiraServerGlobalConfigModel(
            uuid.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            createdAt,
            createdAt,
            url,
            userName,
            password,
            Boolean.FALSE,
            disablePluginCheck
        );
    }

    private FieldModel createGlobalConfig(TestProperties testProperties) {
        Map<String, FieldValueModel> globalConfigFields = Map.of(
            JiraServerDescriptor.KEY_SERVER_URL,
            createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL)),
            JiraServerDescriptor.KEY_SERVER_USERNAME,
            createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME)),
            JiraServerDescriptor.KEY_SERVER_PASSWORD,
            createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD)),
            JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK,
            createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK).orElse("false"))
        );
        return new FieldModel(UUID.randomUUID().toString(), CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), globalConfigFields);
    }

    private Map<String, FieldValueModel> createChannelFieldsMap(TestProperties testProperties) {
        Map<String, FieldValueModel> channelFieldsMap = new HashMap<>();
        channelFieldsMap.put(ChannelDescriptor.KEY_ENABLED, createFieldValueModel("true"));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_NAME, createFieldValueModel(CHANNEL_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_NAME, createFieldValueModel(PERFORMANCE_JOB_NAME));
        channelFieldsMap.put(ChannelDescriptor.KEY_FREQUENCY, createFieldValueModel(FrequencyType.REAL_TIME.name()));
        channelFieldsMap.put(ChannelDescriptor.KEY_PROVIDER_TYPE, createFieldValueModel(PROVIDER_KEY.getUniversalKey()));

        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ADD_COMMENTS, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ADD_COMMENTS).orElse("true")));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ISSUE_CREATOR, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_CREATOR).orElse("")));
        channelFieldsMap.put(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME)));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ISSUE_TYPE, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE).orElse("Task")));
        channelFieldsMap.put(
            JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION,
            createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION).orElse(""))
        );
        channelFieldsMap.put(
            JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
            createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION).orElse(""))
        );

        return channelFieldsMap;
    }

    private FieldValueModel createFieldValueModel(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    private IntegrationPerformanceTestRunnerV2 createTestRunner() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunner.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerV2 configurationManager = new ConfigurationManagerV2(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
        return new IntegrationPerformanceTestRunnerV2(gson, dateTimeFormatter, alertRequestUtility, blackDuckProviderService, configurationManager);
    }
}
