package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.cloud.web.JiraCloudCustomFunctionAction;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManager;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class JiraCloudPerformanceTest {
    private static final String JIRA_CLOUD_PERFORMANCE_JOB_NAME = "Jira Cloud Performance Job";
    private static final JiraCloudChannelKey CHANNEL_KEY = new JiraCloudChannelKey();
    private static final BlackDuckProviderKey PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JiraCloudCustomFunctionAction jiraCloudCustomFunctionAction;

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    @Test
    @Disabled
    public void jiraCloudJobTest() throws Exception {
        TestProperties testProperties = new TestProperties();
        FieldModel globalConfig = createGlobalConfig(testProperties);

        // Install plugin
        ActionResponse<String> actionResponse = jiraCloudCustomFunctionAction.createActionResponse(globalConfig, null);
        if (actionResponse.isError()) {
            fail("Unable to install the Alert plugin for Jira Cloud. Exiting test...");
        }

        Map<String, FieldValueModel> channelFieldsMap = createChannelFieldsMap(testProperties);

        IntegrationPerformanceTestRunner testRunner = createTestRunner();
        testRunner.runTest(globalConfig, channelFieldsMap, JIRA_CLOUD_PERFORMANCE_JOB_NAME);
    }

    private FieldModel createGlobalConfig(TestProperties testProperties) {
        Map<String, FieldValueModel> globalConfigFields = Map.of(
            JiraCloudDescriptor.KEY_JIRA_URL, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_URL)),
            JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_USER_EMAIL)),
            JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_API_TOKEN)),
            JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_DISABLE_PLUGIN_CHECK).orElse("false"))
        );
        return new FieldModel(CHANNEL_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), globalConfigFields);
    }

    private Map<String, FieldValueModel> createChannelFieldsMap(TestProperties testProperties) {
        Map<String, FieldValueModel> channelFieldsMap = new HashMap<>();
        channelFieldsMap.put(ChannelDescriptor.KEY_ENABLED, createFieldValueModel("true"));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_NAME, createFieldValueModel(CHANNEL_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_NAME, createFieldValueModel(JIRA_CLOUD_PERFORMANCE_JOB_NAME));
        channelFieldsMap.put(ChannelDescriptor.KEY_FREQUENCY, createFieldValueModel(FrequencyType.REAL_TIME.name()));
        channelFieldsMap.put(ChannelDescriptor.KEY_PROVIDER_TYPE, createFieldValueModel(PROVIDER_KEY.getUniversalKey()));

        channelFieldsMap.put(JiraCloudDescriptor.KEY_ADD_COMMENTS, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ADD_COMMENTS).orElse("true")));
        channelFieldsMap.put(JiraCloudDescriptor.KEY_ISSUE_CREATOR, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_CREATOR).orElse("")));
        channelFieldsMap.put(JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_PROJECT_NAME)));
        channelFieldsMap.put(JiraCloudDescriptor.KEY_ISSUE_TYPE, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_TYPE).orElse("Task")));
        channelFieldsMap.put(JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_RESOLVE_TRANSITION).orElse("")));
        channelFieldsMap.put(JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_REOPEN_TRANSITION).orElse("")));

        return channelFieldsMap;
    }

    private FieldValueModel createFieldValueModel(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    private IntegrationPerformanceTestRunner createTestRunner() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunner.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManager configurationManager = new ConfigurationManager(gson, alertRequestUtility, blackDuckProviderService.getBlackDuckProviderKey(), CHANNEL_KEY.getUniversalKey());
        return new IntegrationPerformanceTestRunner(gson, dateTimeFormatter, alertRequestUtility, blackDuckProviderService, configurationManager);
    }

}
