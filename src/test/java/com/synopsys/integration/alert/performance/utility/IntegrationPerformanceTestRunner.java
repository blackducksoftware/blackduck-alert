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
import com.synopsys.integration.alert.performance.model.PerformanceExecutionStatusModel;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobCondition;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

public class IntegrationPerformanceTestRunner {
    private static final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(IntegrationPerformanceTestRunner.class));
    private final Gson gson;
    private final DateTimeFormatter dateTimeFormatter;
    private final AlertRequestUtility alertRequestUtility;
    private final BlackDuckProviderService blackDuckProviderService;
    private final ConfigurationManager configurationManager;
    private final PerformanceLoggingUtility loggingUtility;

    private final int waitTimeoutInSeconds;

    public IntegrationPerformanceTestRunner(
        Gson gson,
        DateTimeFormatter dateTimeFormatter,
        AlertRequestUtility alertRequestUtility,
        BlackDuckProviderService blackDuckProviderService,
        ConfigurationManager configurationManager,
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

    public void runTest(Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
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

        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(waitTimeoutInSeconds, 20);
        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
            intLogger,
            startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            waitIntervalTracker
        );
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingSearchDateTime, jobId);
        boolean isComplete = WaitJob.waitFor(resilientJobConfig, notificationWaitJobTask, "int performance test runner notification wait");
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    public PerformanceExecutionStatusModel runTestWithOneJob(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderID,
        List<ProjectVersionWrapper> projectVersionWrappers,
        int numberOfExpectedAuditEntries
    ) {
        LocalDateTime jobStartingTime = LocalDateTime.now();
        LocalDateTime startingNotificationTime;
        String jobId;
        try {
            jobId = configurationManager.createJob(channelFields, jobName, blackDuckProviderID, projectVersionWrappers);
            String jobMessage = String.format("Creating the Job %s jobs took", jobName);
            loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

            startingNotificationTime = LocalDateTime.now();
            // trigger BD notifications
            intLogger.info("Triggered the Black Duck notification.");
            for (ProjectVersionWrapper projectVersionWrapper : projectVersionWrappers) {
                triggerBlackDuckNotification(projectVersionWrapper.getProjectVersionView());
            }
            loggingUtility.logTimeElapsedWithMessage("Triggering all Black Duck notifications took %s", startingNotificationTime, LocalDateTime.now());
        } catch (IntegrationException e) {
            return PerformanceExecutionStatusModel.failure(jobStartingTime, String.format("Failed to create and trigger notification: %s", e));
        }

        AuditProcessingWaitJobTask notificationWaitJobTask = new AuditProcessingWaitJobTask(
            intLogger,
            dateTimeFormatter,
            gson,
            alertRequestUtility,
            startingNotificationTime,
            numberOfExpectedAuditEntries,
            NotificationType.VULNERABILITY,
            Set.of(jobId)
        );
        return waitForJobToFinish(startingNotificationTime, notificationWaitJobTask);
    }

    public PerformanceExecutionStatusModel runPolicyNotificationTest(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderID,
        String policyName,
        int numberOfExpectedAuditEntries,
        boolean waitForAuditComplete
    ) {
        LocalDateTime jobStartingTime = LocalDateTime.now();
        LocalDateTime startingNotificationTime;
        String jobId;
        try {
            jobId = configurationManager.createPolicyViolationJob(channelFields, jobName, blackDuckProviderID);
            String jobMessage = String.format("Creating the Job %s jobs took", jobName);
            loggingUtility.logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

            startingNotificationTime = LocalDateTime.now();
            // trigger BD notifications
            intLogger.info("Triggered the Black Duck notification.");
            triggerBlackDuckPolicyNotification(policyName);
            loggingUtility.logTimeElapsedWithMessage("Triggering policy notification took %s", startingNotificationTime, LocalDateTime.now());
        } catch (IntegrationException e) {
            return PerformanceExecutionStatusModel.failure(jobStartingTime, String.format("Failed to create and trigger notification: %s", e));
        }

        WaitJobCondition waitJobCondition = createWaitJobCondition(
            blackDuckProviderID,
            Set.of(jobId),
            startingNotificationTime,
            numberOfExpectedAuditEntries
        );
        return waitForJobToFinish(startingNotificationTime, waitJobCondition);
    }

    public PerformanceExecutionStatusModel testManyPolicyJobsToManyProjects(
        Set<String> jobIds,
        String blackDuckProviderID,
        String policyName,
        int numberOfExpectedAuditEntries,
        boolean waitForAuditComplete
    ) {
        // trigger BD notifications
        LocalDateTime startingNotificationTime = LocalDateTime.now();
        try {
            intLogger.info("Triggered the Black Duck notification.");
            triggerBlackDuckPolicyNotification(policyName);
            loggingUtility.logTimeElapsedWithMessage("Triggering policy notification took %s", startingNotificationTime, LocalDateTime.now());
        } catch (IntegrationException e) {
            return PerformanceExecutionStatusModel.failure(startingNotificationTime, String.format("Failed trigger notification: %s", e));
        }

        WaitJobCondition waitJobCondition = createWaitJobCondition(
            blackDuckProviderID,
            jobIds,
            startingNotificationTime,
            numberOfExpectedAuditEntries
        );
        return waitForJobToFinish(startingNotificationTime, waitJobCondition);
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

    private PerformanceExecutionStatusModel waitForJobToFinish(LocalDateTime startingNotificationTime, WaitJobCondition waitJobCondition) {
        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(waitTimeoutInSeconds, 2);
        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
            intLogger,
            startingNotificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            waitIntervalTracker
        );
        try {
            boolean isComplete = WaitJob.waitFor(resilientJobConfig, waitJobCondition, "int performance test runner notification wait");
            intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
            assertTrue(isComplete);
            return PerformanceExecutionStatusModel.success(startingNotificationTime);
        } catch (IntegrationException e) {
            return PerformanceExecutionStatusModel.failure(startingNotificationTime, String.format("An error occurred while waiting for jobs to complete: %s", e));
        } catch (InterruptedException e) {
            return PerformanceExecutionStatusModel.failure(startingNotificationTime, String.format("Performance job interrupted with error: %s", e));
        }
    }

    private WaitJobCondition createWaitJobCondition(
        String blackDuckProviderId,
        Set<String> jobIds,
        LocalDateTime startingNotificationTime,
        int numberOfExpectedAuditEntries
    ) {
        return new ProcessingCompleteWaitJobTask(
            intLogger,
            gson,
            alertRequestUtility,
            startingNotificationTime,
            numberOfExpectedAuditEntries,
            jobIds,
            Long.valueOf(blackDuckProviderId)
        );
    }
}
