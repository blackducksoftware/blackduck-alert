package com.blackduck.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.performance.model.PerformanceExecutionStatusModel;
import com.blackduck.integration.alert.performance.utility.BlackDuckProviderService;
import com.blackduck.integration.alert.performance.utility.ConfigurationManager;
import com.blackduck.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.blackduck.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.blackduck.integration.alert.performance.utility.PerformanceLoggingUtility;
import com.blackduck.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
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
class ScalingExternalPerformanceTest {
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final int DEFAULT_NUMBER_OF_JOBS_TO_CREATE = 10;
    private static final int DEFAULT_TIMEOUT_SECONDS = 14400;
    private static final String PERFORMANCE_POLICY_NAME = "PerformanceTestPolicy";

    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();
    private final PerformanceLoggingUtility loggingUtility = new PerformanceLoggingUtility(intLogger, dateTimeFormatter);

    private final IntHttpClient client = new IntHttpClient(intLogger, gson, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final TestProperties testProperties = new TestProperties();

    private BlackDuckProviderService blackDuckProviderService;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunner testRunner;
    private ConfigurationManager configurationManager;

    private int numberOfProjectsToCreate;
    private int numberOfJobsToCreate;

    @BeforeEach
    public void init() throws IntegrationException {
        String alertURL = testProperties.getProperty(TestPropertyKey.TEST_PERFORMANCE_ALERT_SERVER_URL);
        numberOfProjectsToCreate = Integer.parseInt(testProperties.getProperty(TestPropertyKey.TEST_PERFORMANCE_BLACKDUCK_PROJECT_COUNT));
        numberOfJobsToCreate = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_ALERT_DISTRIBUTION_JOB_COUNT)
            .map(Integer::parseInt)
            .orElse(DEFAULT_NUMBER_OF_JOBS_TO_CREATE);

        int waitTimeoutSeconds = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_WAIT_TIMEOUT_SECONDS)
            .map(Integer::parseInt)
            .orElse(DEFAULT_TIMEOUT_SECONDS);

        ExternalAlertRequestUtility alertRequestUtility = new ExternalAlertRequestUtility(intLogger, client, alertURL);
        alertRequestUtility.loginToExternalAlert();
        blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        configurationManager = new ConfigurationManager(
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
    void testScalingAlertPerformance() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        loggingUtility.logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(jiraServerGlobalConfigModel);
        loggingUtility
            .logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

        // Clear existing policies
        PolicyRuleView policyRuleView = blackDuckProviderService.createBlackDuckPolicyRuleView(PERFORMANCE_POLICY_NAME, BlackDuckProviderService.getDefaultExternalIdSupplier());
        blackDuckProviderService.deleteExistingBlackDuckPolicy(policyRuleView);

        LocalDateTime jobStartingTime = LocalDateTime.now();
        Set<String> policyJobIds = new HashSet<>();
        for (int i = 1; i <= numberOfJobsToCreate; i++) {
            String jobName = String.format("JiraPerformanceJob-%s", i);
            intLogger.info(String.format("Creating distribution job: %s", jobName));
            Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, jobName, globalConfiguration.getId());
            policyJobIds.add(configurationManager.createPolicyViolationJob(channelFieldsMap, jobName, blackDuckProviderID));
        }
        String jobMessage = String.format("Creating %s jobs took", numberOfJobsToCreate);
        assertEquals(policyJobIds.size(), numberOfJobsToCreate, "The number of jobs to create does not match the set of jobIds that were created.");
        loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime executionStartTime = LocalDateTime.now();
        PerformanceExecutionStatusModel performanceExecutionStatusModel = testRunner
            .testManyPolicyJobsToManyProjects(policyJobIds, blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, true);

        loggingUtility.logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        loggingUtility.logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());

        if (performanceExecutionStatusModel.isFailure()) {
            intLogger.info(String.format("An error occurred while testing: %s", performanceExecutionStatusModel.getMessage()));
        }
        assertTrue(performanceExecutionStatusModel.isSuccess());
    }
}
