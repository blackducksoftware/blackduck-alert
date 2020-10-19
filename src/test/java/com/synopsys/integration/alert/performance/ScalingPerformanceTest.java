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
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
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

        // TODO delete existing jobs?
        List<String> jobIds = new ArrayList<>();
        startingTime = LocalDateTime.now();
        // create 10 slack jobs, trigger notification, and wait for all 10 to succeed
        createAndTestJobs(startingTime, jobIds, 10);

        startingTime = LocalDateTime.now();
        // create 90 more slack jobs, trigger notification, and wait for all 100 to succeed
        createAndTestJobs(startingTime, jobIds, 90);

        // TODO create 900 more slack jobs for a total of 1000
        // TODO create 1000 more slack jobs for a total of 2000
    }

    private void createAndTestJobs(LocalDateTime startingTime, List<String> jobIds, int numberOfJobsToCreate) throws Exception {
        // create slack jobs
        createSlackJobs(startingTime, jobIds, numberOfJobsToCreate, 10);

        LocalDateTime startingNotificationSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        triggerBlackDuckNotification();
        logTimeElapsedWithMessage("Triggering the Black Duck notification took %s", startingNotificationSearchDateTime, LocalDateTime.now());

        LocalDateTime startingNotificationWaitForTenJobs = LocalDateTime.now();
        // check that all jobs have processed the notification successfully, log how long it took
        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, getDateTimeFormatter(), getGson(), getAlertRequestUtility(), startingNotificationSearchDateTime, jobIds);
        notificationWaitJobTask.setFailOnJobFailure(false);
        WaitJob waitForNotificationToBeProcessed = WaitJob.create(intLogger, 900, startingNotificationSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 30, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        logTimeElapsedWithMessage("Waiting for " + numberOfJobsToCreate + " jobs to process the notification took %s", startingNotificationWaitForTenJobs, LocalDateTime.now());

        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    private void createSlackJobs(LocalDateTime startingTime, List<String> jobIds, int numberOfJobsToCreate, int intervalToLog) throws Exception {
        int startingJobNum = jobIds.size();

        while (jobIds.size() < startingJobNum + numberOfJobsToCreate) {

            // Create a Slack Job with a unique name using the job number
            Integer jobNumber = jobIds.size();
            jobIds.add(createSlackJob(jobNumber));

            if (jobIds.size() % intervalToLog == 0) {
                String message = String.format("Creating %s jobs took", jobIds.size() - startingJobNum);
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalDateTime.now());
            }
        }
        intLogger.info(String.format("Finished creating %s jobs. Current Job number %s.", numberOfJobsToCreate, jobIds.size()));
    }

    private String createSlackJob(Integer jobNumber) throws Exception {
        String jobName = String.format("%s #%s", SLACK_SCALING_PERFORMANCE_JOB_NAME, jobNumber);

        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(List.of(blackDuckProviderID), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, new FieldValueModel(List.of("BOM_EDIT", "POLICY_OVERRIDE", "RULE_VIOLATION", "RULE_VIOLATION_CLEARED", "VULNERABILITY"), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProjectName()), true));
        FieldModel jobProviderConfiguration = new FieldModel(getBlackDuckProperties().getBlackDuckProviderKey(), ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        Map<String, FieldValueModel> slackKeyToValues = new HashMap<>();
        slackKeyToValues.put(ChannelDistributionUIConfig.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        slackKeyToValues.put(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, new FieldValueModel(List.of(slackProperties.getSlackChannelKey()), true));
        slackKeyToValues.put(ChannelDistributionUIConfig.KEY_NAME, new FieldValueModel(List.of(jobName), true));
        slackKeyToValues.put(ChannelDistributionUIConfig.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackKeyToValues.put(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProviderKey()), true));

        slackKeyToValues.put(SlackDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(slackProperties.getSlackChannelWebhook()), true));
        slackKeyToValues.put(SlackDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(slackProperties.getSlackChannelName()), true));
        slackKeyToValues.put(SlackDescriptor.KEY_CHANNEL_USERNAME, new FieldValueModel(List.of(slackProperties.getSlackChannelUsername()), true));

        FieldModel jobSlackConfiguration = new FieldModel(slackProperties.getSlackChannelKey(), ConfigContextEnum.DISTRIBUTION.name(), slackKeyToValues);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobSlackConfiguration, jobProviderConfiguration));

        String jobConfigBody = getGson().toJson(jobFieldModel);

        //TODO from my initial investigation, every job you that is created slows down the validation/job creation of the next job
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
