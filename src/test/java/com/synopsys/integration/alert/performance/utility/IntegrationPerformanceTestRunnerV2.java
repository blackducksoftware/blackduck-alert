package com.synopsys.integration.alert.performance.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.wait.WaitJob;
import com.synopsys.integration.wait.WaitJobConfig;

public class IntegrationPerformanceTestRunnerV2 {
    private static final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(IntegrationPerformanceTestRunner.class));
    private final Gson gson;
    private final DateTimeFormatter dateTimeFormatter;
    private final AlertRequestUtility alertRequestUtility;
    private final BlackDuckProviderService blackDuckProviderService;
    private final ConfigurationManagerV2 configurationManager;

    public IntegrationPerformanceTestRunnerV2(
        Gson gson,
        DateTimeFormatter dateTimeFormatter,
        AlertRequestUtility alertRequestUtility,
        BlackDuckProviderService blackDuckProviderService,
        ConfigurationManagerV2 configurationManager
    ) {
        this.gson = gson;
        this.dateTimeFormatter = dateTimeFormatter;
        this.alertRequestUtility = alertRequestUtility;
        this.blackDuckProviderService = blackDuckProviderService;
        this.configurationManager = configurationManager;
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

    public <T extends Obfuscated<T>> void runTest(T configModel, Class<T> modelType, Map<String, FieldValueModel> channelFields, String jobName)
        throws IntegrationException, InterruptedException {
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(LocalDateTime.now())));

        String blackDuckProviderID = createBlackDuckConfiguration();

        String apiConfigurationPath = AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH;
        Optional<T> globalConfiguration = createGlobalConfiguration(apiConfigurationPath, modelType, configModel);

        if (globalConfiguration.isEmpty()) {
            //TODO: Return a test failure and pass to the performance tetstt
            fail("Global configuraton missing.");
        }

        //TODO: get the globalConfiguration, find the ID and add it to the distribution job in the 'channelFields'

        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobId = configurationManager.createJob(channelFields, jobName, blackDuckProviderID, blackDuckProviderService.getBlackDuckProjectName());
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime startingSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        blackDuckProviderService.triggerBlackDuckNotification();
        intLogger.info("Triggered the Black Duck notification.");

        WaitJobConfig waitJobConfig = new WaitJobConfig(
            intLogger,
            "int performance test runner notification wait",
            600,
            startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            20
        );
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingSearchDateTime, jobId);
        WaitJob<Boolean> waitForNotificationToBeProcessed = WaitJob.createSimpleWait(waitJobConfig, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    //TODO: Disabled for testing
    /*
    public void runTest(FieldModel channelGlobalConfig, Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
        runTest(() -> createGlobalConfiguration(channelGlobalConfig), channelFields, jobName);
    }

    public void runTest(Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
        runTest(() -> {}, channelFields, jobName);
    }*/

    /*
    public void runTest(Runnable createGlobalConfigFunction, Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(LocalDateTime.now())));

        String blackDuckProviderID = createBlackDuckConfiguration();

        createGlobalConfigFunction.run();

        LocalDateTime jobStartingTime = LocalDateTime.now();
        String jobId = configurationManager.createJob(channelFields, jobName, blackDuckProviderID, blackDuckProviderService.getBlackDuckProjectName());
        String jobMessage = String.format("Creating the Job %s jobs took", jobName);
        logTimeElapsedWithMessage(jobMessage + " %s", jobStartingTime, LocalDateTime.now());

        LocalDateTime startingSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        blackDuckProviderService.triggerBlackDuckNotification();
        intLogger.info("Triggered the Black Duck notification.");

        WaitJobConfig waitJobConfig = new WaitJobConfig(intLogger, "int performance test runner notification wait", 600, startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 20);
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingSearchDateTime, jobId);
        WaitJob<Boolean> waitForNotificationToBeProcessed = WaitJob.createSimpleWait(waitJobConfig, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }
     */

    private String createBlackDuckConfiguration() {
        LocalDateTime startingTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        return blackDuckProviderID;
    }

    private <T extends Obfuscated<T>> Optional<T> createGlobalConfiguration(String apiConfigurationPath, Class<T> modelType, T channelGlobalConfig) {
        if (null != channelGlobalConfig) {
            LocalDateTime startingTime = LocalDateTime.now();
            //String descriptorName = channelGlobalConfig.getDescriptorName();
            return configurationManager.createGlobalConfiguration(apiConfigurationPath, modelType, channelGlobalConfig);
            //String globalConfigMessage = String.format("Creating the global Configuration for %s jobs took", descriptorName);
            //logTimeElapsedWithMessage(globalConfigMessage + " %s", startingTime, LocalDateTime.now());
        }
        return Optional.empty();
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

}
