package com.synopsys.integration.alert.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerV2;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerV2;
import com.synopsys.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
import com.synopsys.integration.alert.test.common.TestProperties;
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
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();
    private static final int NUMBER_OF_PROJECTS_TO_CREATE = 5000;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = IntegrationPerformanceTestRunnerV2.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerV2.createDateTimeFormatter();

    private BlackDuckProviderService blackDuckProviderService;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunnerV2 testRunner;

    @BeforeEach
    public void init() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunnerV2.createAlertRequestUtility(webApplicationContext);
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
    @Disabled("This test is used to populate a blackduck server with notifications and populate the blackduck database.")
    void createProjectsAndNotificationsTest() throws IntegrationException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        // Create Black Duck Global Provider configuration
        LocalDateTime startingProviderCreateTime = LocalDateTime.now();
        blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Setting up the Black Duck provider took %s", startingProviderCreateTime, LocalDateTime.now());

        // create 10 blackduck projects
        List<ProjectVersionWrapper> projectVersionWrappers = createBlackDuckProjects(NUMBER_OF_PROJECTS_TO_CREATE);

        //trigger a notification on each project
        for (ProjectVersionWrapper projectVersionWrapper : projectVersionWrappers) {
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    @Test
        //@Disabled("Used for performance testing only.")
        //TODO: Note, this test is enabled for overnight performance testing. It should be disabled again before merging into master
    void largeNotificationTest() throws IntegrationException, InterruptedException {
        LocalDateTime startingTime = LocalDateTime.now();
        logger.info(String.format("Starting time: %s", dateTimeFormatter.format(startingTime)));

        TestProperties testProperties = new TestProperties();
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
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, globalConfiguration.getId());

        // Create N number of blackduck projects
        List<ProjectVersionWrapper> projectVersionWrappers = createBlackDuckProjects(NUMBER_OF_PROJECTS_TO_CREATE);

        LocalDateTime executionStartTime = LocalDateTime.now();
        testRunner.runTestWithOneJob(channelFieldsMap, "performanceJob", blackDuckProviderID, projectVersionWrappers);

        logTimeElapsedWithMessage("Execution and processing test time: %s", executionStartTime, LocalDateTime.now());
        logTimeElapsedWithMessage("Total test time: %s", startingTime, LocalDateTime.now());
    }

    private List<ProjectVersionWrapper> createBlackDuckProjects(int numberOfProjects) throws IntegrationException {
        LocalDateTime startingProjectCreationTime = LocalDateTime.now();
        List<ProjectVersionWrapper> projectVersionWrappers = new ArrayList<>();

        for (int projectIndex = 0; projectIndex < numberOfProjects; projectIndex++) {
            projectVersionWrappers.add(blackDuckProviderService.findOrCreateBlackDuckProjectAndVersion(String.format("AlertPerformanceProject-%s", projectIndex), "version1"));
        }
        String createProjectsLogMessage = String.format("Creating %s projects took", numberOfProjects);
        logTimeElapsedWithMessage(String.format("%s %s", createProjectsLogMessage, "%s"), startingProjectCreationTime, LocalDateTime.now());
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

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
