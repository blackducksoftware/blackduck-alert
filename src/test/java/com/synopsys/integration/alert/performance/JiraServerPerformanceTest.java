package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
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
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.synopsys.integration.exception.IntegrationException;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class JiraServerPerformanceTest {
    private static final String PERFORMANCE_JOB_NAME = "Jira Server Performance Job";
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final BlackDuckProviderKey PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerV2.createDateTimeFormatter();

    private AlertRequestUtility alertRequestUtility;
    private BlackDuckProviderService blackDuckProviderService;
    private ConfigurationManagerV2 configurationManager;

    @BeforeEach
    public void init() {
        alertRequestUtility = IntegrationPerformanceTestRunnerV2.createAlertRequestUtility(webApplicationContext);
        blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        configurationManager = new ConfigurationManagerV2(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
    }

    @Test
    @Disabled("Used for performance testing only.")
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void jiraServerJobTest() throws Exception {
        TestProperties testProperties = new TestProperties();
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createGlobalConfigModel(testProperties);

        ValidationResponseModel installPluginResponse = installPlugin(jiraServerGlobalConfigModel);
        if (installPluginResponse.hasErrors()) {
            fail("Unable to install the Alert plugin for Jira Server. Exiting test...");
        }

        Optional<JiraServerGlobalConfigModel> globalConfiguration = createGlobalConfiguration(jiraServerGlobalConfigModel);
        if (globalConfiguration.isEmpty()) {
            fail("Global configuration missing.");
        }

        Map<String, FieldValueModel> channelFieldsMap = createChannelFieldsMap(testProperties, globalConfiguration.get().getId());
        IntegrationPerformanceTestRunnerV2 testRunner = new IntegrationPerformanceTestRunnerV2(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );
        testRunner.runTest(channelFieldsMap, PERFORMANCE_JOB_NAME);
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

    private Map<String, FieldValueModel> createChannelFieldsMap(TestProperties testProperties, String globalConfigId) {
        Map<String, FieldValueModel> channelFieldsMap = new HashMap<>();
        channelFieldsMap.put(ChannelDescriptor.KEY_ENABLED, createFieldValueModel("true"));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_NAME, createFieldValueModel(CHANNEL_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_NAME, createFieldValueModel(PERFORMANCE_JOB_NAME));
        channelFieldsMap.put(ChannelDescriptor.KEY_FREQUENCY, createFieldValueModel(FrequencyType.REAL_TIME.name()));
        channelFieldsMap.put(ChannelDescriptor.KEY_PROVIDER_TYPE, createFieldValueModel(PROVIDER_KEY.getUniversalKey()));
        channelFieldsMap.put(ChannelDescriptor.KEY_CHANNEL_GLOBAL_CONFIG_ID, createFieldValueModel(globalConfigId));

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

    private Optional<JiraServerGlobalConfigModel> createGlobalConfiguration(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        String apiConfigurationPath = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;
        if (null != jiraServerGlobalConfigModel) {
            LocalDateTime startingTime = LocalDateTime.now();
            String descriptorName = CHANNEL_KEY.getUniversalKey();
            Optional<JiraServerGlobalConfigModel> globalConfigModel = configurationManager
                .createGlobalConfiguration(apiConfigurationPath, jiraServerGlobalConfigModel, JiraServerGlobalConfigModel.class);
            String globalConfigMessage = String.format("Creating the global Configuration for %s jobs took", descriptorName);
            logTimeElapsedWithMessage(globalConfigMessage + " %s", startingTime, LocalDateTime.now());
            return globalConfigModel;
        }
        return Optional.empty();
    }

    private ValidationResponseModel installPlugin(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        try {
            String requestBody = gson.toJson(jiraServerGlobalConfigModel);
            String installPluginResponseString = alertRequestUtility.executePostRequest(
                String.format("%s/install-plugin", AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH),
                requestBody,
                "Installing the plugin failed."
            );
            return gson.fromJson(installPluginResponseString, ValidationResponseModel.class);
        } catch (IntegrationException e) {
            logger.error("Unexpected error occurred while installing the plugin.", e);
            return ValidationResponseModel.generalError(e.getMessage());
        }
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
