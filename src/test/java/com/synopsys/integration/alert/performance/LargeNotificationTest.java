package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = IntegrationPerformanceTestRunnerV2.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerV2.createDateTimeFormatter();

    private AlertRequestUtility alertRequestUtility;
    private BlackDuckProviderService blackDuckProviderService;
    private ConfigurationManagerV2 configurationManager;
    private JiraServerPerformanceUtility jiraServerPerformanceUtility;

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
        jiraServerPerformanceUtility = new JiraServerPerformanceUtility(alertRequestUtility, configurationManager);
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
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModel(testProperties);

        // Create Black Duck Global Provider configuration
        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        // Create Jira Server global config
        ValidationResponseModel installPluginResponse = jiraServerPerformanceUtility.installPlugin(jiraServerGlobalConfigModel);
        if (installPluginResponse.hasErrors()) {
            fail("Unable to install the Alert plugin for Jira Server. Exiting test...");
        }

        Optional<JiraServerGlobalConfigModel> globalConfiguration = jiraServerPerformanceUtility.createGlobalConfiguration(jiraServerGlobalConfigModel);
        if (globalConfiguration.isEmpty()) {
            fail("Global configuration missing.");
        }

        // Create distribution job
        // TODO: Look into adding this feature in IntegrationPerformanceTestRunnerV2
        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, globalConfiguration.get().getId());

        // Create N number of blackduck projects
        List<ProjectVersionWrapper> projectVersionWrappers = createBlackDuckProjects(10);

        IntegrationPerformanceTestRunnerV2 testRunner = new IntegrationPerformanceTestRunnerV2(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );
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

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
