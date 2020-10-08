package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.performance.model.SlackPerformanceProperties;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.NotificationWaitJobTask;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.wait.WaitJob;

public class ScalingPerformanceTest extends BasePerformanceTest {
    private final static String SLACK_SCALING_PERFORMANCE_JOB_NAME = "Slack Scaling Performance Job";
    private final SlackPerformanceProperties slackProperties = new SlackPerformanceProperties();

    private final IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final String alertURL = "https://localhost:8443/alert";

    private String blackDuckProviderID = "-1";

    @Test
    @Ignore
    public void testAlertPerformance() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time %s", getDateTimeFormatter().format(startingTime)));

        // Create an authenticated connection to Alert
        getExternalAlertRequestUtility().loginToExternalAlert();

        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        // Create the Black Duck Global provider configuration
        blackDuckProviderID = setupBlackDuck();

        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        List<String> jobIds = new ArrayList<>();
        // create 10 slack jobs
        createSlackJobs(startingTime, jobIds, 10, 10);

        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        triggerBlackDuckNotification();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        LocalDateTime startingNotificationWaitForTenJobs = LocalDateTime.now();
        // check that all jobs have processed the notification successfully, log how long it took
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, getDateTimeFormatter(), getGson(), getAlertRequestUtility(), startingNotificationSearchDateTime, jobIds);
        WaitJob waitForNotificationToBeProcessed = WaitJob.create(intLogger, 600, startingNotificationSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 20, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        logTimeElapsedWithMessage("Waiting for 10 jobs to process the notification took %s", startingNotificationWaitForTenJobs, LocalDateTime.now());

        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);

        // TODO create 90 more slack jobs for a total of 100
        // trigger BD notification
        //triggerBlackDuckNotification();
        // TODO check that all jobs have processed the notification, log how long it took

        // TODO create 900 more slack jobs for a total of 1000
        // trigger BD notification
        //triggerBlackDuckNotification();
        // TODO check that all jobs have processed the notification, log how long it took

        // TODO create 1000 more slack jobs for a total of 2000
        // trigger BD notification
        //triggerBlackDuckNotification();
        // TODO check that all jobs have processed the notification, log how long it took
    }

    private void createSlackJobs(LocalDateTime startingTime, List<String> jobIds, int numberOfJobsToCreate, int intervalToLog) throws Exception {
        int startingJobNum = jobIds.size();

        while (jobIds.size() < startingJobNum + numberOfJobsToCreate) {

            // Create a Slack Job with a unique name using the job number
            Integer jobNumber = jobIds.size();
            jobIds.add(createSlackJob(jobNumber));

            if (jobIds.size() % intervalToLog == 0) {
                String message = String.format("Creating %s jobs took", jobIds.size());
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalDateTime.now());
            }
        }
        intLogger.info(String.format("Finished creating %s jobs. Current Job number %s.", numberOfJobsToCreate, jobIds.size()));
    }

    private String createSlackJob(Integer jobNumber) throws Exception {
        String jobName = String.format("%s #%s", SLACK_SCALING_PERFORMANCE_JOB_NAME, jobNumber);

        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put("provider.common.config.id", new FieldValueModel(List.of(blackDuckProviderID), true));
        providerKeyToValues.put("provider.distribution.notification.types", new FieldValueModel(List.of("BOM_EDIT", "POLICY_OVERRIDE", "RULE_VIOLATION", "RULE_VIOLATION_CLEARED", "VULNERABILITY"), true));
        providerKeyToValues.put("provider.distribution.processing.type", new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put("channel.common.filter.by.project", new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put("channel.common.configured.project", new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProjectName()), true));
        FieldModel jobProviderConfiguration = new FieldModel(getBlackDuckProperties().getBlackDuckProviderKey(), ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        Map<String, FieldValueModel> slackKeyToValues = new HashMap<>();
        slackKeyToValues.put("channel.common.enabled", new FieldValueModel(List.of("true"), true));
        slackKeyToValues.put("channel.common.channel.name", new FieldValueModel(List.of(slackProperties.getSlackChannelKey()), true));
        slackKeyToValues.put("channel.common.name", new FieldValueModel(List.of(jobName), true));
        slackKeyToValues.put("channel.common.frequency", new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackKeyToValues.put("channel.common.provider.name", new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProviderKey()), true));

        slackKeyToValues.put("channel.slack.webhook", new FieldValueModel(List.of(slackProperties.getSlackChannelWebhook()), true));
        slackKeyToValues.put("channel.slack.channel.name", new FieldValueModel(List.of(slackProperties.getSlackChannelName()), true));
        slackKeyToValues.put("channel.slack.channel.username", new FieldValueModel(List.of(slackProperties.getSlackChannelUsername()), true));

        FieldModel jobSlackConfiguration = new FieldModel(slackProperties.getSlackChannelKey(), ConfigContextEnum.DISTRIBUTION.name(), slackKeyToValues);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobSlackConfiguration, jobProviderConfiguration));

        String jobConfigBody = getGson().toJson(jobFieldModel);

        getAlertRequestUtility().executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", jobName));
        // executePostRequest("/api/configuration/job/test", requestBody, String.format("Testing the Slack Job #%s failed.", jobNumber));
        String creationResponse = getAlertRequestUtility().executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", jobName));

        JsonObject jsonObject = getGson().fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

    @Override
    public AlertRequestUtility createAlertRequestUtility() {
        return new ExternalAlertRequestUtility(intLogger, client, alertURL);
    }

    public ExternalAlertRequestUtility getExternalAlertRequestUtility() {
        return (ExternalAlertRequestUtility) getAlertRequestUtility();
    }
}
