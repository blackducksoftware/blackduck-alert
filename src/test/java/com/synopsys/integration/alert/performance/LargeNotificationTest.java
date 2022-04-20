package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerV2;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class LargeNotificationTest {
    private static final String PERFORMANCE_JOB_NAME = "Jira Server Performance Job";
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final BlackDuckProviderKey PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = IntegrationPerformanceTestRunnerV2.createGson();
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
    void createProjectsAndNotificationsTest() throws IntegrationException {
        // Create Black Duck Global Provider configuration
        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        // create 10 blackduck projects
        List<ProjectVersionWrapper> projectVersionWrappers = createBlackDuckProjects(10);

        //trigger a notification on each project
        for (ProjectVersionWrapper projectVersionWrapper : projectVersionWrappers) {
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
    }

    @Test
    @Disabled("Used for performance testing only.")
    void largeNotificationTest() throws IntegrationException, InterruptedException {
        LocalDateTime startingTime = LocalDateTime.now();
        TestProperties testProperties = new TestProperties();
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createGlobalConfigModel(testProperties);

        // Create Black Duck Global Provider configuration
        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        // Create Jira Server global config
        ValidationResponseModel installPluginResponse = installPlugin(jiraServerGlobalConfigModel);
        if (installPluginResponse.hasErrors()) {
            fail("Unable to install the Alert plugin for Jira Server. Exiting test...");
        }

        Optional<JiraServerGlobalConfigModel> globalConfiguration = createGlobalConfiguration(jiraServerGlobalConfigModel);
        if (globalConfiguration.isEmpty()) {
            fail("Global configuration missing.");
        }

        // Create distribution job
        // TODO: Look into adding this feature in IntegrationPerformanceTestRunnerV2
        Map<String, FieldValueModel> channelFieldsMap = createChannelFieldsMap(testProperties, globalConfiguration.get().getId());
        /*
        Map<String, FieldValueModel> channelFieldsMap = createChannelFieldsMap(testProperties, globalConfiguration.get().getId());
        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobName = "JiraServerPerformanceJob";
        String jobId = configurationManager.createJob(channelFieldsMap, jobName, blackDuckProviderID, blackDuckProviderService.getBlackDuckProjectName());
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());
*/
        // Create N number of blackduck projects
        List<ProjectVersionWrapper> projectVersionWrappers = createBlackDuckProjects(10);

        IntegrationPerformanceTestRunnerV2 testRunner = new IntegrationPerformanceTestRunnerV2(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );
        //testRunner.runTestWithOneJob(jobId, projectVersionViews);
        testRunner.runTestWithOneJob(channelFieldsMap, "performanceJob", blackDuckProviderID, projectVersionWrappers);
    }

    private List<ProjectVersionWrapper> createBlackDuckProjects(int numberOfProjects) throws IntegrationException {
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        List<ProjectVersionWrapper> projectVersionWrappers = new ArrayList<>();

        for (int projectIndex = 0; projectIndex < numberOfProjects; projectIndex++) {
            projectVersionWrappers.add(blackDuckProviderService.findOrCreateBlackDuckProjectAndVersion(String.format("AlertPerformanceProject-%s", projectIndex), "version1"));
        }
        logTimeElapsedWithMessage("Creating projects took %s", startingProjectCreationTime, LocalDateTime.now());
        return projectVersionWrappers;
    }

    private void triggerBlackDuckNotification(ProjectVersionView projectVersionView) throws IntegrationException {
        LocalDateTime startingNotificationTriggerDateTime = LocalDateTime.now();
        blackDuckProviderService.triggerBlackDuckNotificationForProjectVersion(
            projectVersionView,
            BlackDuckProviderService.getDefaultExternalIdSupplier(),
            BlackDuckProviderService.getDefaultBomComponentFilter()
        );
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationTriggerDateTime, LocalDateTime.now());
    }

    //TODO: put into utility class
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
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, createFieldValueModel(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME)));
        channelFieldsMap
            .put(JiraServerDescriptor.KEY_ISSUE_TYPE, createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE).orElse("Task")));
        channelFieldsMap
            .put(
                JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION,
                createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION).orElse(""))
            );
        channelFieldsMap
            .put(
                JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION,
                createFieldValueModel(testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION).orElse(""))
            );

        return channelFieldsMap;
    }

    //TODO: put into utility class
    private FieldValueModel createFieldValueModel(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    //TODO: put into utility class
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

    //TODO: put into utility class
    private JiraServerGlobalConfigModel createGlobalConfigModel(TestProperties testProperties) {
        String createdAt = OffsetDateTime.now().toString();
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
        String userName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
        String password = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);
        Boolean disablePluginCheck = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK)
            .map(Boolean::valueOf)
            .orElse(Boolean.FALSE);

        return new JiraServerGlobalConfigModel(
            null,
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

    //TODO: put into utility class
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

    //TODO: put into utility class
    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
