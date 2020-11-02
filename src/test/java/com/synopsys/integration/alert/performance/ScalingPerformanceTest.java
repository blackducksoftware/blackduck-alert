package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.performance.utility.NotificationWaitJobTask;
import com.synopsys.integration.alert.performance.utility.TestJobCreator;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.wait.WaitJob;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public class ScalingPerformanceTest {
    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final static String SLACK_SCALING_PERFORMANCE_JOB_NAME = "Slack Scaling Performance Job";

    private final IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final String alertURL = "https://localhost:8443/alert";

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    private String blackDuckProviderID = "-1";

    private static String SLACK_CHANNEL_KEY;
    private static String SLACK_CHANNEL_WEBHOOK;
    private static String SLACK_CHANNEL_NAME;
    private static String SLACK_CHANNEL_USERNAME;

    @BeforeAll
    public static void initTest() {
        SLACK_CHANNEL_KEY = new SlackChannelKey().getUniversalKey();

        TestProperties testProperties = new TestProperties();
        SLACK_CHANNEL_WEBHOOK = testProperties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        SLACK_CHANNEL_NAME = testProperties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME);
        SLACK_CHANNEL_USERNAME = testProperties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
    }

    @Test
    @Ignore
    @Disabled
    public void testAlertPerformance() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(startingTime)));

        ExternalAlertRequestUtility alertRequestUtility = new ExternalAlertRequestUtility(intLogger, client, alertURL);
        // Create an authenticated connection to Alert
        alertRequestUtility.loginToExternalAlert();
        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalDateTime.now());

        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        TestJobCreator testJobCreator = new TestJobCreator(gson, alertRequestUtility, blackDuckProviderService.getBlackDuckProviderKey(), SLACK_CHANNEL_KEY);

        startingTime = LocalDateTime.now();
        // Create the Black Duck Global provider configuration
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());

        List<String> jobIds = new ArrayList<>();
        startingTime = LocalDateTime.now();
        // create 10 slack jobs, trigger notification, and wait for all 10 to succeed
        createAndTestJobs(alertRequestUtility, blackDuckProviderService, testJobCreator, startingTime, jobIds, 10, blackDuckProviderID);

        startingTime = LocalDateTime.now();
        // create 90 more slack jobs, trigger notification, and wait for all 100 to succeed
        createAndTestJobs(alertRequestUtility, blackDuckProviderService, testJobCreator, startingTime, jobIds, 90, blackDuckProviderID);

        // TODO create 900 more slack jobs for a total of 1000
        // TODO create 1000 more slack jobs for a total of 2000
    }

    private void createAndTestJobs(AlertRequestUtility alertRequestUtility, BlackDuckProviderService blackDuckProviderService, TestJobCreator testJobCreator, LocalDateTime startingTime, List<String> jobIds, int numberOfJobsToCreate,
        String blackDuckProviderID) throws Exception {
        // create slack jobs
        createSlackJobs(testJobCreator, startingTime, jobIds, numberOfJobsToCreate, 10, blackDuckProviderID, blackDuckProviderService.getBlackDuckProviderKey());

        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        blackDuckProviderService.triggerBlackDuckNotification();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        LocalDateTime startingNotificationWaitForTenJobs = LocalDateTime.now();
        // check that all jobs have processed the notification successfully, log how long it took
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingNotificationSearchDateTime, jobIds);
        notificationWaitJobTask.setFailOnJobFailure(false);
        WaitJob waitForNotificationToBeProcessed = WaitJob.create(intLogger, 900, startingNotificationSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 30, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        logTimeElapsedWithMessage("Waiting for " + numberOfJobsToCreate + " jobs to process the notification took %s", startingNotificationWaitForTenJobs, LocalDateTime.now());

        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    private void createSlackJobs(TestJobCreator testJobCreator, LocalDateTime startingTime, List<String> jobIds, int numberOfJobsToCreate, int intervalToLog, String blackDuckProviderID, String blackDuckProviderKey) throws Exception {
        int startingJobNum = jobIds.size();

        while (jobIds.size() < startingJobNum + numberOfJobsToCreate) {

            // Create a Slack Job with a unique name using the job number
            Integer jobNumber = jobIds.size();

            String jobName = String.format("%s #%s", SLACK_SCALING_PERFORMANCE_JOB_NAME, jobNumber);
            Map<String, FieldValueModel> slackKeyToValues = new HashMap<>();
            slackKeyToValues.put(ChannelDistributionUIConfig.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
            slackKeyToValues.put(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, new FieldValueModel(List.of(SLACK_CHANNEL_KEY), true));
            slackKeyToValues.put(ChannelDistributionUIConfig.KEY_NAME, new FieldValueModel(List.of(jobName), true));
            slackKeyToValues.put(ChannelDistributionUIConfig.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
            slackKeyToValues.put(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, new FieldValueModel(List.of(blackDuckProviderKey), true));

            slackKeyToValues.put(SlackDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(SLACK_CHANNEL_WEBHOOK), true));
            slackKeyToValues.put(SlackDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(SLACK_CHANNEL_NAME), true));
            slackKeyToValues.put(SlackDescriptor.KEY_CHANNEL_USERNAME, new FieldValueModel(List.of(SLACK_CHANNEL_USERNAME), true));
            String jobId = testJobCreator.createJob(slackKeyToValues, jobName, blackDuckProviderID, blackDuckProviderKey);
            jobIds.add(jobId);

            if (jobIds.size() % intervalToLog == 0) {
                String message = String.format("Creating %s jobs took", jobIds.size() - startingJobNum);
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalDateTime.now());
            }
        }
        intLogger.info(String.format("Finished creating %s jobs. Current Job number %s.", numberOfJobsToCreate, jobIds.size()));
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

}
