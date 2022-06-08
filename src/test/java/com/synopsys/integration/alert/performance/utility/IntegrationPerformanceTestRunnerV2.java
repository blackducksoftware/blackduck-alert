package com.synopsys.integration.alert.performance.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobCondition;

public class IntegrationPerformanceTestRunnerV2 {
    private static final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(IntegrationPerformanceTestRunnerV2.class));
    private final Gson gson;
    private final DateTimeFormatter dateTimeFormatter;
    private final AlertRequestUtility alertRequestUtility;
    private final BlackDuckProviderService blackDuckProviderService;
    private final ConfigurationManagerV2 configurationManager;
    private final PerformanceLoggingUtility loggingUtility;

    private final int waitTimeoutInSeconds;

    public IntegrationPerformanceTestRunnerV2(
        Gson gson,
        DateTimeFormatter dateTimeFormatter,
        AlertRequestUtility alertRequestUtility,
        BlackDuckProviderService blackDuckProviderService,
        ConfigurationManagerV2 configurationManager,
        int waitTimeoutInSeconds
    ) {
        this.gson = gson;
        this.dateTimeFormatter = dateTimeFormatter;
        this.alertRequestUtility = alertRequestUtility;
        this.blackDuckProviderService = blackDuckProviderService;
        this.configurationManager = configurationManager;
        this.waitTimeoutInSeconds = waitTimeoutInSeconds;
        loggingUtility = new PerformanceLoggingUtility(intLogger, dateTimeFormatter);
    }

    public static AlertRequestUtility createAlertRequestUtility(WebApplicationContext webApplicationContext) {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        return new IntegrationAlertRequestUtility(intLogger, mockMvc);
    }

    public static Gson createGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    public static DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    }

    public void runTest(Map<String, FieldValueModel> channelFields, String jobName)
        throws IntegrationException, InterruptedException {
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(LocalDateTime.now())));

        String blackDuckProviderID = createBlackDuckConfiguration();

        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobId = configurationManager.createJob(channelFields, jobName, blackDuckProviderID, blackDuckProviderService.getBlackDuckProjectName());
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime startingSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        blackDuckProviderService.triggerBlackDuckNotification();
        intLogger.info("Triggered the Black Duck notification.");

        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
            intLogger,
            waitTimeoutInSeconds,
            startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            20
        );
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingSearchDateTime, jobId);
        boolean isComplete = WaitJob.waitFor(resilientJobConfig, notificationWaitJobTask, "int performance test runner notification wait");
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    public void runTestWithOneJob(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderID,
        List<ProjectVersionWrapper> projectVersionWrappers,
        int numberOfExpectedAuditEntries
    )
        throws IntegrationException, InterruptedException {
        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobId = configurationManager.createJob(channelFields, jobName, blackDuckProviderID, projectVersionWrappers);
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime startingNotificationTime = LocalDateTime.now();
        // trigger BD notifications
        intLogger.info("Triggered the Black Duck notification.");
        for (ProjectVersionWrapper projectVersionWrapper : projectVersionWrappers) {
            triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
        }
        loggingUtility.logTimeElapsedWithMessage("Triggering all Black Duck notifications took %s", startingNotificationTime, LocalDateTime.now());

        waitForJobToFinish(Set.of(jobId), startingNotificationTime, numberOfExpectedAuditEntries, NotificationType.VULNERABILITY);
    }

    public void runPolicyNotificationTest(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderID,
        String policyName,
        int numberOfExpectedAuditEntries,
        boolean waitForAuditComplete
    )
        throws IntegrationException, InterruptedException {
        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobId = configurationManager.createPolicyViolationJob(channelFields, jobName, blackDuckProviderID);
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime startingNotificationTime = LocalDateTime.now();
        // trigger BD notifications
        intLogger.info("Triggered the Black Duck notification.");
        triggerBlackDuckPolicyNotification(policyName);
        loggingUtility.logTimeElapsedWithMessage("Triggering policy notification took %s", startingNotificationTime, LocalDateTime.now());

        WaitJobCondition waitJobCondition;
        if (waitForAuditComplete) {
            waitJobCondition = createAuditCompleteWaitTask(Set.of(jobId), startingNotificationTime, numberOfExpectedAuditEntries, NotificationType.RULE_VIOLATION);
        } else {
            waitJobCondition = createAuditProcessingWaitTask(Set.of(jobId), startingNotificationTime, numberOfExpectedAuditEntries, NotificationType.RULE_VIOLATION);
        }
        waitForJobToFinish(startingNotificationTime, waitJobCondition);
    }

    public void testManyPolicyJobsToManyProjects(
        Set<String> jobIds,
        String policyName,
        int numberOfExpectedAuditEntries
    )
        throws IntegrationException, InterruptedException {
        // trigger BD notifications
        LocalDateTime startingNotificationTime = LocalDateTime.now();
        intLogger.info("Triggered the Black Duck notification.");
        triggerBlackDuckPolicyNotification(policyName);
        loggingUtility.logTimeElapsedWithMessage("Triggering policy notification took %s", startingNotificationTime, LocalDateTime.now());

        waitForJobToFinish(jobIds, startingNotificationTime, numberOfExpectedAuditEntries, NotificationType.RULE_VIOLATION);
    }

    private void triggerBlackDuckNotification(ProjectVersionView projectVersionView) throws IntegrationException {
        LocalDateTime startingNotificationTriggerDateTime = LocalDateTime.now();
        blackDuckProviderService.triggerBlackDuckNotificationForProjectVersion(
            projectVersionView,
            BlackDuckProviderService.getDefaultExternalIdSupplier(),
            BlackDuckProviderService.getDefaultBomComponentFilter()
        );
        loggingUtility.logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationTriggerDateTime, LocalDateTime.now());
    }

    private void triggerBlackDuckPolicyNotification(String policyName) throws IntegrationException {
        LocalDateTime startingNotificationTriggerDateTime = LocalDateTime.now();
        blackDuckProviderService.triggerBlackDuckPolicyNotification(policyName, BlackDuckProviderService.getDefaultExternalIdSupplier());
        loggingUtility.logTimeElapsedWithMessage("Triggering the Black Duck policy notification took %s", startingNotificationTriggerDateTime, LocalDateTime.now());
    }

    private String createBlackDuckConfiguration() {
        LocalDateTime startingTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        loggingUtility.logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        return blackDuckProviderID;
    }

    private void waitForJobToFinish(Set<String> jobIds, LocalDateTime startingNotificationTime, int numberOfExpectedAuditEntries, NotificationType notificationType)
        throws IntegrationException, InterruptedException {
        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
            intLogger,
            waitTimeoutInSeconds,
            startingNotificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            2
        );
        AuditProcessingWaitJobTask notificationWaitJobTask = new AuditProcessingWaitJobTask(
            intLogger,
            dateTimeFormatter,
            gson,
            alertRequestUtility,
            startingNotificationTime,
            numberOfExpectedAuditEntries,
            notificationType,
            jobIds
        );
        boolean isComplete = WaitJob.waitFor(resilientJobConfig, notificationWaitJobTask, "int performance test runner notification wait");
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    private void waitForJobToFinish(LocalDateTime startingNotificationTime, WaitJobCondition waitJobCondition)
        throws IntegrationException, InterruptedException {
        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
            intLogger,
            waitTimeoutInSeconds,
            startingNotificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            2
        );
        boolean isComplete = WaitJob.waitFor(resilientJobConfig, waitJobCondition, "int performance test runner notification wait");
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    private AuditCompleteWaitJobTask createAuditCompleteWaitTask(
        Set<String> jobIds,
        LocalDateTime startingNotificationTime,
        int numberOfExpectedAuditEntries,
        NotificationType notificationType
    ) {
        return new AuditCompleteWaitJobTask(
            intLogger,
            dateTimeFormatter,
            gson,
            alertRequestUtility,
            startingNotificationTime,
            numberOfExpectedAuditEntries,
            notificationType,
            jobIds
        );
    }

    private AuditProcessingWaitJobTask createAuditProcessingWaitTask(
        Set<String> jobIds,
        LocalDateTime startingNotificationTime,
        int numberOfExpectedAuditEntries,
        NotificationType notificationType
    ) {
        return new AuditProcessingWaitJobTask(
            intLogger,
            dateTimeFormatter,
            gson,
            alertRequestUtility,
            startingNotificationTime,
            numberOfExpectedAuditEntries,
            notificationType,
            jobIds
        );
    }
}
