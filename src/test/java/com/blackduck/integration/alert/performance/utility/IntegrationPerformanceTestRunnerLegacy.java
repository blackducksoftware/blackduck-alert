/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.wait.ResilientJobConfig;
import com.blackduck.integration.wait.WaitJob;
import com.blackduck.integration.wait.tracker.WaitIntervalTracker;
import com.blackduck.integration.wait.tracker.WaitIntervalTrackerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//TODO: Implementations of this class should be replaced with IntegrationPerformanceTestRunner
@Deprecated(forRemoval = true)
public class IntegrationPerformanceTestRunnerLegacy {
    private static final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(IntegrationPerformanceTestRunnerLegacy.class));
    private final Gson gson;
    private final DateTimeFormatter dateTimeFormatter;
    private final AlertRequestUtility alertRequestUtility;
    private final BlackDuckProviderService blackDuckProviderService;
    private final ConfigurationManagerLegacy configurationManager;

    public IntegrationPerformanceTestRunnerLegacy(
        Gson gson,
        DateTimeFormatter dateTimeFormatter,
        AlertRequestUtility alertRequestUtility,
        BlackDuckProviderService blackDuckProviderService,
        ConfigurationManagerLegacy configurationManager
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

    public void runTest(FieldModel channelGlobalConfig, Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
        runTest(() -> createGlobalConfiguration(channelGlobalConfig), channelFields, jobName);
    }

    public void runTest(Map<String, FieldValueModel> channelFields, String jobName) throws IntegrationException, InterruptedException {
        runTest(() -> {}, channelFields, jobName);
    }

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
        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(600, 20);
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

    private String createBlackDuckConfiguration() {
        LocalDateTime startingTime = LocalDateTime.now();
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        return blackDuckProviderID;
    }

    private void createGlobalConfiguration(FieldModel channelGlobalConfig) {
        if (null != channelGlobalConfig) {
            LocalDateTime startingTime = LocalDateTime.now();
            String descriptorName = channelGlobalConfig.getDescriptorName();
            configurationManager.createGlobalConfiguration(channelGlobalConfig);
            String globalConfigMessage = String.format("Creating the global Configuration for %s jobs took", descriptorName);
            logTimeElapsedWithMessage(globalConfigMessage + " %s", startingTime, LocalDateTime.now());
        }
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

}
