package com.synopsys.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.performance.model.SlackPerformanceProperties;
import com.synopsys.integration.alert.performance.utility.NotificationWaitJobTask;
import com.synopsys.integration.wait.WaitJob;

public class SlackPerformanceTest extends IntegrationPerformanceTest {
    private final static String SLACK_PERFORMANCE_JOB_NAME = "Slack Performance Job";
    private final SlackPerformanceProperties slackProperties = new SlackPerformanceProperties();

    private String blackDuckProviderID = "-1";

    @Autowired
    DescriptorConfigRepository descriptorConfigRepository;

    @Autowired
    RegisteredDescriptorRepository registeredDescriptorRepository;

    @Autowired
    FieldValueRepository fieldValueRepository;

    private Long getDescriptorIdOrThrowException(String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorRepository
                   .findFirstByName(descriptorName)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor with the provided name was registered"));
    }

    @Test
    @Ignore
    public void testAlertPerformance() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time %s", getDateTimeFormatter().format(startingTime)));

        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        // Create the Black Duck Global provider configuration
        blackDuckProviderID = setupBlackDuck();

        fieldValueRepository.flush();
        descriptorConfigRepository.flush();

        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        String jobId = createSlackJob();
        String message = String.format("Creating the Job %s jobs took", SLACK_PERFORMANCE_JOB_NAME);
        logTimeElapsedWithMessage(message + " %s", startingTime, LocalDateTime.now());

        LocalDateTime startingSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        triggerBlackDuckNotification();

        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, getDateTimeFormatter(), getGson(), getAlertRequestUtility(), startingSearchDateTime, jobId);
        WaitJob waitForNotificationToBeProcessed = WaitJob.create(intLogger, 600, startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 20, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();
        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);
        assertTrue(isComplete);
    }

    private String createSlackJob() throws Exception {
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
        slackKeyToValues.put("channel.common.name", new FieldValueModel(List.of(SLACK_PERFORMANCE_JOB_NAME), true));
        slackKeyToValues.put("channel.common.frequency", new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackKeyToValues.put("channel.common.provider.name", new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProviderKey()), true));

        slackKeyToValues.put("channel.slack.webhook", new FieldValueModel(List.of(slackProperties.getSlackChannelWebhook()), true));
        slackKeyToValues.put("channel.slack.channel.name", new FieldValueModel(List.of(slackProperties.getSlackChannelName()), true));
        slackKeyToValues.put("channel.slack.channel.username", new FieldValueModel(List.of(slackProperties.getSlackChannelUsername()), true));

        FieldModel jobSlackConfiguration = new FieldModel(slackProperties.getSlackChannelKey(), ConfigContextEnum.DISTRIBUTION.name(), slackKeyToValues);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobSlackConfiguration, jobProviderConfiguration));

        String jobConfigBody = getGson().toJson(jobFieldModel);

        getAlertRequestUtility().executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", SLACK_PERFORMANCE_JOB_NAME));
        // executePostRequest("/api/configuration/job/test", requestBody, String.format("Testing the Slack Job #%s failed.", jobNumber));
        String creationResponse = getAlertRequestUtility().executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", SLACK_PERFORMANCE_JOB_NAME));

        JsonObject jsonObject = getGson().fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

}
