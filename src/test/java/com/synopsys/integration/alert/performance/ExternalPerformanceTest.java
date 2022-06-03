package com.synopsys.integration.alert.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerV2;
import com.synopsys.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerV2;
import com.synopsys.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Tag(TestTags.DEFAULT_PERFORMANCE)
class ExternalPerformanceTest {
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final int DEFAULT_NUMBER_OF_PROJECTS_TO_CREATE = 10;
    private static final String PERFORMANCE_POLICY_NAME = "PerformanceTestPolicy";
    private static final String DEFAULT_JOB_NAME = "JiraPerformanceJob";

    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final Gson gson = IntegrationPerformanceTestRunnerV2.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerV2.createDateTimeFormatter();

    private final IntHttpClient client = new IntHttpClient(intLogger, gson, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final TestProperties testProperties = new TestProperties();

    private BlackDuckProviderService blackDuckProviderService;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunnerV2 testRunner;

    private int numberOfProjectsToCreate;

    private boolean disablePluginCheck;

    @BeforeEach
    public void init() throws IntegrationException {
        String alertURL = testProperties.getProperty(TestPropertyKey.TEST_PERFORMANCE_ALERT_SERVER_URL);
        numberOfProjectsToCreate = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_BLACKDUCK_PROJECT_COUNT)
            .map(Integer::parseInt)
            .orElse(DEFAULT_NUMBER_OF_PROJECTS_TO_CREATE);

        disablePluginCheck = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK)
            .map(Boolean::parseBoolean)
            .orElse(Boolean.FALSE);

        ExternalAlertRequestUtility alertRequestUtility = new ExternalAlertRequestUtility(intLogger, client, alertURL);
        alertRequestUtility.loginToExternalAlert();
        blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerV2 configurationManager = new ConfigurationManagerV2(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
        jiraServerPerformanceUtility = new JiraServerPerformanceUtility(alertRequestUtility, configurationManager);
        testRunner = new IntegrationPerformanceTestRunnerV2(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void testPolicyNotificationsWithExternalAlertServer() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        boolean installPlugin = !disablePluginCheck;
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(installPlugin, jiraServerGlobalConfigModel);
        logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

        // Create distribution job fields
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, DEFAULT_JOB_NAME, globalConfiguration.getId());

        // Clear existing policies
        PolicyRuleView policyRuleView = blackDuckProviderService.createBlackDuckPolicyRuleView(PERFORMANCE_POLICY_NAME, BlackDuckProviderService.getDefaultExternalIdSupplier());
        blackDuckProviderService.deleteExistingBlackDuckPolicy(policyRuleView);

        // Create N number of Blackduck projects and add a vulnerable component to each
        // Note: Setup for this test can take anywhere from 5-20 minutes depending on the instance of Blackduck. By pre-populating the server
        //  with projects and components using 'createProjectsAndNotificationsTest' the code below can be skipped.
        /*
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        for (int index = 1; index <= NUMBER_OF_PROJECTS_TO_CREATE; index++) {
            ProjectVersionWrapper projectVersionWrapper = createBlackDuckProject(index);
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
        String createProjectsLogMessage = String.format("Creating %s projects took", NUMBER_OF_PROJECTS_TO_CREATE);
        logTimeElapsedWithMessage(String.format("%s %s", createProjectsLogMessage, "%s"), startingProjectCreationTime, LocalDateTime.now());
        */
        LocalDateTime executionStartTime = LocalDateTime.now();
        testRunner.runPolicyNotificationTest(channelFieldsMap, "performanceJob", blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, false);

        logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void testPolicyNotificationsValidateAuditComplete() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        boolean installPlugin = !disablePluginCheck;
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(installPlugin, jiraServerGlobalConfigModel);
        logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

        // Create distribution job fields
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, DEFAULT_JOB_NAME, globalConfiguration.getId());

        // Clear existing policies
        PolicyRuleView policyRuleView = blackDuckProviderService.createBlackDuckPolicyRuleView(PERFORMANCE_POLICY_NAME, BlackDuckProviderService.getDefaultExternalIdSupplier());
        blackDuckProviderService.deleteExistingBlackDuckPolicy(policyRuleView);

        LocalDateTime executionStartTime = LocalDateTime.now();
        testRunner.runPolicyNotificationTest(channelFieldsMap, "performanceJob", blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, true);

        logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}


