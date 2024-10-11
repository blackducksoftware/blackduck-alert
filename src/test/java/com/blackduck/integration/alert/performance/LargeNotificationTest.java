/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.Application;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.configuration.ApplicationConfiguration;
import com.blackduck.integration.alert.database.DatabaseDataSource;
import com.blackduck.integration.alert.performance.model.PerformanceExecutionStatusModel;
import com.blackduck.integration.alert.performance.utility.AlertRequestUtility;
import com.blackduck.integration.alert.performance.utility.BlackDuckProviderService;
import com.blackduck.integration.alert.performance.utility.ConfigurationManager;
import com.blackduck.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.blackduck.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.DescriptorMocker;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import com.google.gson.Gson;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class LargeNotificationTest {
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final int DEFAULT_NUMBER_OF_PROJECTS_TO_CREATE = 10;
    private static final int DEFAULT_STARTING_PROJECT_NUMBER = 1;
    private static final String PERFORMANCE_POLICY_NAME = "PerformanceTestPolicy";
    private static final String DEFAULT_JOB_NAME = "JiraPerformanceJob";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    private BlackDuckProviderService blackDuckProviderService;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunner testRunner;

    private TestProperties testProperties = new TestProperties();
    private int numberOfProjectsToCreate;

    @BeforeEach
    public void init() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunner.createAlertRequestUtility(webApplicationContext);
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
            14400
        );

        numberOfProjectsToCreate = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_BLACKDUCK_PROJECT_COUNT)
            .map(Integer::parseInt)
            .orElse(DEFAULT_NUMBER_OF_PROJECTS_TO_CREATE);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void createTestProjects() throws IntegrationException {
        int startingProjectNumber = testProperties.getOptionalProperty(TestPropertyKey.TEST_PERFORMANCE_BLACKDUCK_START_PROJECT_NUMBER)
            .map(Integer::parseInt)
            .orElse(DEFAULT_STARTING_PROJECT_NUMBER);

        if (startingProjectNumber < DEFAULT_STARTING_PROJECT_NUMBER) {
            logger.info(String.format("Starting project number invalid. Setting to default: %s", DEFAULT_STARTING_PROJECT_NUMBER));
            startingProjectNumber = DEFAULT_STARTING_PROJECT_NUMBER;
        }

        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // create blackduck projects
        createProjectAndTriggerNotification(numberOfProjectsToCreate, startingProjectNumber);

        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void createProjectsAndNotificationsTest() throws IntegrationException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // create blackduck projects
        createBlackDuckProjects(numberOfProjectsToCreate);

        //trigger a notification on each project
        for (int index = 0; index < numberOfProjectsToCreate; index++) {
            ProjectVersionWrapper projectVersionWrapper = findOrCreateBlackDuckProject(index);
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void deleteProjectsAndVersionsTest() throws IntegrationException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // create  blackduck projects
        deleteBlackDuckProjects(numberOfProjectsToCreate);
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void largeVulnerabilityNotificationTest() throws IntegrationException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(jiraServerGlobalConfigModel);
        logTimeElapsedWithMessage("Installing the jira server plugin and creating global configuration took %s", startingCreateGlobalConfigTime, LocalDateTime.now());

        // Create distribution job fields
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, DEFAULT_JOB_NAME, globalConfiguration.getId());

        // Create N number of blackduck projects
        createBlackDuckProjects(numberOfProjectsToCreate);
        List<ProjectVersionWrapper> projectVersionWrappers = new LinkedList<>();
        for (int index = 0; index < numberOfProjectsToCreate; index++) {
            projectVersionWrappers.add(findOrCreateBlackDuckProject(index));
        }

        LocalDateTime executionStartTime = LocalDateTime.now();
        PerformanceExecutionStatusModel performanceExecutionStatusModel = testRunner
            .runTestWithOneJob(channelFieldsMap, "performanceJob", blackDuckProviderID, projectVersionWrappers, numberOfProjectsToCreate);

        logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());

        if (performanceExecutionStatusModel.isFailure()) {
            logger.info(String.format("An error occurred while testing: %s", performanceExecutionStatusModel.getMessage()));
        }
        assertTrue(performanceExecutionStatusModel.isSuccess());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ALERT_RUN_PERFORMANCE", matches = "true")
    void largePolicyNotificationTest() throws IntegrationException, InterruptedException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // Create Jira Server global config
        LocalDateTime startingCreateGlobalConfigTime = LocalDateTime.now();
        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(jiraServerGlobalConfigModel);
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
        PerformanceExecutionStatusModel performanceExecutionStatusModel = testRunner
            .runPolicyNotificationTest(channelFieldsMap, "performanceJob", blackDuckProviderID, PERFORMANCE_POLICY_NAME, numberOfProjectsToCreate, true);

        logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());

        if (performanceExecutionStatusModel.isFailure()) {
            logger.info(String.format("An error occurred while testing: %s", performanceExecutionStatusModel.getMessage()));
        }
        assertTrue(performanceExecutionStatusModel.isSuccess());
    }

    private ProjectVersionWrapper findOrCreateBlackDuckProject(int index) throws IntegrationException {
        return blackDuckProviderService.findOrCreateBlackDuckProjectAndVersion(String.format("AlertPerformanceProject-%s", index), "version1");
    }

    private void createBlackDuckProjects(int numberOfProjects, int startingProjectNumber) throws IntegrationException {
        logger.info(String.format("Creating %s projects, starting with project number %s", numberOfProjects, startingProjectNumber));
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        for (int projectIndex = startingProjectNumber; projectIndex < (numberOfProjects + startingProjectNumber); projectIndex++) {
            findOrCreateBlackDuckProject(projectIndex);
        }
        String createProjectsLogMessage = String.format("Creating %s projects took", numberOfProjects);
        logTimeElapsedWithMessage(String.format("%s %s", createProjectsLogMessage, "%s"), startingProjectCreationTime, LocalDateTime.now());
    }

    private void createBlackDuckProjects(int numberOfProjects) throws IntegrationException {
        createBlackDuckProjects(numberOfProjects, DEFAULT_STARTING_PROJECT_NUMBER);
    }

    private void createProjectAndTriggerNotification(int numberOfProjects, int startingProjectNumber) throws IntegrationException {
        logger.info(String.format("Creating and triggering a notification for %s projects, starting with project number %s", numberOfProjects, startingProjectNumber));
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        for (int projectIndex = startingProjectNumber; projectIndex < (numberOfProjects + startingProjectNumber); projectIndex++) {
            ProjectVersionWrapper projectVersionWrapper = findOrCreateBlackDuckProject(projectIndex);
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
        String createProjectsLogMessage = String.format("Creating and triggering a notification for %s projects took", numberOfProjects);
        logTimeElapsedWithMessage(String.format("%s %s", createProjectsLogMessage, "%s"), startingProjectCreationTime, LocalDateTime.now());
    }

    private void deleteBlackDuckProjects(int numberOfProjects, int startingProjectNumber) throws IntegrationException {
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        for (int projectIndex = startingProjectNumber; projectIndex < (numberOfProjects + startingProjectNumber); projectIndex++) {
            blackDuckProviderService.deleteBlackDuckProjectAndVersion(String.format("AlertPerformanceProject-%s", projectIndex), "version1");
        }
        String createProjectsLogMessage = String.format("Deleting %s projects took", numberOfProjects);
        logTimeElapsedWithMessage(String.format("%s %s", createProjectsLogMessage, "%s"), startingProjectCreationTime, LocalDateTime.now());
    }

    private void deleteBlackDuckProjects(int numberOfProjects) throws IntegrationException {
        deleteBlackDuckProjects(numberOfProjects, DEFAULT_STARTING_PROJECT_NUMBER);
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

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
