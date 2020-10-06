package com.synopsys.integration.alert;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public class AlertSetupTest {
    private final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private final String blackDuckProviderKey = new BlackDuckProviderKey().getUniversalKey();
    private final String blackDuckProviderUrl = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_URL");
    private final String blackDuckApiToken = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_API_KEY");
    private final String blackDuckTimeout = System.getenv("ALERT_PROVIDER_BLACKDUCK_BLACKDUCK_TIMEOUT");
    private final String blackDuckProviderName = blackDuckProviderUrl + UUID.randomUUID();

    private final String slackChannelKey = new SlackChannelKey().getUniversalKey();
    private final String slackChannelWebhook = System.getenv("ALERT_CHANNEL_SLACK_SLACK_WEBHOOK");
    private final String slackChannelName = System.getenv("ALERT_CHANNEL_SLACK_SLACK_CHANNEL_NAME");
    private final String slackChannelUsername = System.getenv("ALERT_CHANNEL_SLACK_SLACK_CHANNEL_USERNAME");
    private final String slackJobProviderProjectPattern = System.getenv("ALERT_CHANNEL_COMMON_PROJECT_NAME_PATTERN");

    private final String alertURL = "https://localhost:8443/alert/";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);

    private String blackDuckProviderID = "-1";

    @Test
    @Ignore
    public void testConfigureAlert() throws Exception {
        LocalTime startingTime = LocalTime.now();
        loginToAlert();
        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalTime.now());
        startingTime = LocalTime.now();
        setupBlackDuck();
        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalTime.now());
        startingTime = LocalTime.now();
        int currentJobNum = 1;
        while (currentJobNum < 2001) {
            setupSlackJob(currentJobNum);
            if (currentJobNum % 100 == 0) {
                String message = String.format("Creating %s jobs took", currentJobNum);
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalTime.now());
            }
            currentJobNum++;
        }
    }

    private void logTimeElapsedWithMessage(String messageFormat, LocalTime start, LocalTime end) {
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
    }

    private void loginToAlert() throws Exception {
        String loginBody = "{\"alertUsername\":\"sysadmin\",\"alertPassword\":\"blackduck\"}";
        BodyContent requestBody = new StringBodyContent(loginBody);
        Response response = executePostRequest("api/login", requestBody, "Could not log into Alert.");

        String csrfToken = response.getHeaderValue("X-CSRF-TOKEN");
        String cookie = response.getHeaderValue("Set-Cookie");
        client.addCommonRequestHeader("X-CSRF-TOKEN", csrfToken);
        client.addCommonRequestHeader("Cookie", cookie);
        intLogger.info("Logged into Alert.");
    }

    private void setupBlackDuck() throws Exception {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put("provider.common.config.enabled", new FieldValueModel(List.of("true"), true));
        keyToValues.put("provider.common.config.name", new FieldValueModel(List.of(blackDuckProviderName), true));
        keyToValues.put("blackduck.url", new FieldValueModel(List.of(blackDuckProviderUrl), true));
        keyToValues.put("blackduck.api.key", new FieldValueModel(List.of(blackDuckApiToken), true));
        keyToValues.put("blackduck.timeout", new FieldValueModel(List.of(blackDuckTimeout), true));
        FieldModel blackDuckProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.GLOBAL.name(), keyToValues);

        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);
        BodyContent requestBody = new StringBodyContent(blackDuckConfigBody);

        executePostRequest("api/configuration/validate", requestBody, "Validating the Black Duck provider failed.");
        executePostRequest("api/configuration/test", requestBody, "Testing the Black Duck provider failed.");
        Response creationResponse = executePostRequest("api/configuration", requestBody, "Could not create the Black Duck provider.");

        JsonObject jsonObject = gson.fromJson(creationResponse.getContentString(), JsonObject.class);
        blackDuckProviderID = jsonObject.get("id").getAsString();
        intLogger.info(String.format("Configured the Black Duck provider, ID %s.", blackDuckProviderID));
    }

    private void setupSlackJob(Integer jobNumber) throws Exception {
        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put("provider.common.config.id", new FieldValueModel(List.of(blackDuckProviderID), true));
        providerKeyToValues.put("provider.distribution.notification.types", new FieldValueModel(List.of("BOM_EDIT", "POLICY_OVERRIDE", "RULE_VIOLATION", "RULE_VIOLATION_CLEARED", "VULNERABILITY"), true));
        providerKeyToValues.put("provider.distribution.processing.type", new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put("channel.common.filter.by.project", new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put("channel.common.project.name.pattern", new FieldValueModel(List.of(slackJobProviderProjectPattern), true));
        FieldModel jobProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        Map<String, FieldValueModel> slackKeyToValues = new HashMap<>();
        slackKeyToValues.put("channel.common.enabled", new FieldValueModel(List.of("true"), true));
        slackKeyToValues.put("channel.common.channel.name", new FieldValueModel(List.of(slackChannelKey), true));
        slackKeyToValues.put("channel.common.name", new FieldValueModel(List.of("Slack Job #" + jobNumber), true));
        slackKeyToValues.put("channel.common.frequency", new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackKeyToValues.put("channel.common.provider.name", new FieldValueModel(List.of(blackDuckProviderKey), true));

        slackKeyToValues.put("channel.slack.webhook", new FieldValueModel(List.of(slackChannelWebhook), true));
        slackKeyToValues.put("channel.slack.channel.name", new FieldValueModel(List.of(slackChannelName), true));
        slackKeyToValues.put("channel.slack.channel.username", new FieldValueModel(List.of(slackChannelUsername), true));

        FieldModel jobSlackConfiguration = new FieldModel(slackChannelKey, ConfigContextEnum.DISTRIBUTION.name(), slackKeyToValues);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobSlackConfiguration, jobProviderConfiguration));

        String jobConfigBody = gson.toJson(jobFieldModel);
        BodyContent requestBody = new StringBodyContent(jobConfigBody);

        executePostRequest("api/configuration/job/validate", requestBody, String.format("Validating the Slack Job #%s failed.", jobNumber));
        // executePostRequest("api/configuration/job/test", requestBody, String.format("Testing the Slack Job #%s failed.", jobNumber));
        executePostRequest("api/configuration/job", requestBody, String.format("Could not create the Slack Job #%s.", jobNumber));
    }

    private Response executePostRequest(String path, BodyContent requestBody, String error) throws Exception {
        Request.Builder requestBuilder = createRequestBuilder(path);
        requestBuilder.method(HttpMethod.POST);
        requestBuilder.bodyContent(requestBody);
        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error(error);
            response.throwExceptionForError();
        }
        return response;
    }

    private Request.Builder createRequestBuilder(String path) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(new HttpUrl(alertURL + path));
        return requestBuilder;
    }

}
