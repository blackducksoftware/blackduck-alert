package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.synopsys.integration.alert.performance.model.PerformanceExecutionStatusModel;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManager;
import com.synopsys.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.performance.utility.PerformanceLoggingUtility;
import com.synopsys.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;

@Tag(TestTags.DEFAULT_PERFORMANCE)
class ExternalPerformanceTest {
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final int DEFAULT_NUMBER_OF_PROJECTS_TO_CREATE = 10;
    private static final int DEFAULT_TIMEOUT_SECONDS = 14400;
    private static final String PERFORMANCE_POLICY_NAME = "PerformanceTestPolicy";
    private static final String DEFAULT_JOB_NAME = "JiraPerformanceJob";

    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();
    private final PerformanceLoggingUtility loggingUtility = new PerformanceLoggingUtility(intLogger, dateTimeFormatter);

    private final IntHttpClient client = new IntHttpClient(intLogger, gson, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final TestProperties testProperties = new TestProperties();

    private BlackDuckProviderService blackDuckProviderService;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunner testRunner;
    private ExternalAlertRequestUtility alertRequestUtility;

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

        int waitTimeoutSeconds = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_WAIT_TIMEOUT_SECONDS)
            .map(Integer::parseInt)
            .orElse(DEFAULT_TIMEOUT_SECONDS);

        alertRequestUtility = new ExternalAlertRequestUtility(intLogger, client, alertURL);
        blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManager configurationManager = new ConfigurationManager(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
        jiraServerPerformanceUtility = new JiraServerPerformanceUtility(alertRequestUtility, configurationManager);
        testRunner = new IntegrationPerformanceTestRunner(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager,
            waitTimeoutSeconds
        );
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void testPolicyNotificationsWithExternalAlertServer() throws Exception {
        alertRequestUtility.loginToExternalAlert();

        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        loggingUtility.logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        boolean installPlugin = !disablePluginCheck;
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(installPlugin, jiraServerGlobalConfigModel);
        loggingUtility
            .logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

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
        PerformanceExecutionStatusModel performanceExecutionStatusModel = testRunner
            .runPolicyNotificationTest(channelFieldsMap, "performanceJob", blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, false);

        loggingUtility.logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        loggingUtility.logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());

        if (performanceExecutionStatusModel.isFailure()) {
            intLogger.info(String.format("An error occurred while testing: %s", performanceExecutionStatusModel.getMessage()));
        }
        assertTrue(performanceExecutionStatusModel.isSuccess());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void testPolicyNotificationsValidateAuditComplete() throws Exception {
        alertRequestUtility.loginToExternalAlert();

        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        loggingUtility.logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        boolean installPlugin = !disablePluginCheck;
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(installPlugin, jiraServerGlobalConfigModel);
        loggingUtility
            .logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

        // Create distribution job fields
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, DEFAULT_JOB_NAME, globalConfiguration.getId());

        // Clear existing policies
        PolicyRuleView policyRuleView = blackDuckProviderService.createBlackDuckPolicyRuleView(PERFORMANCE_POLICY_NAME, BlackDuckProviderService.getDefaultExternalIdSupplier());
        blackDuckProviderService.deleteExistingBlackDuckPolicy(policyRuleView);

        LocalDateTime executionStartTime = LocalDateTime.now();
        PerformanceExecutionStatusModel performanceExecutionStatusModel = testRunner
            .runPolicyNotificationTest(channelFieldsMap, "performanceJob", blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, true);

        loggingUtility.logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        loggingUtility.logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());

        if (performanceExecutionStatusModel.isFailure()) {
            intLogger.info(String.format("An error occurred while testing: %s", performanceExecutionStatusModel.getMessage()));
        }
        assertTrue(performanceExecutionStatusModel.isSuccess());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void testDeleteExistingPolicy() throws IntegrationException {
        PolicyRuleView policyRuleView = blackDuckProviderService.createBlackDuckPolicyRuleView(PERFORMANCE_POLICY_NAME, BlackDuckProviderService.getDefaultExternalIdSupplier());
        blackDuckProviderService.deleteExistingBlackDuckPolicy(policyRuleView);
    }
}


