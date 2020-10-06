package com.synopsys.integration.alert;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
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
    private final String blackDuckProviderUrl = System.getenv("TEST_BLACKDUCK_URL");
    private final String blackDuckApiToken = System.getenv("TEST_BLACKDUCK_API_KEY");
    private final String blackDuckTimeout = System.getenv("TEST_BLACKDUCK_TIMEOUT");
    private final String blackDuckProviderName = blackDuckProviderUrl + UUID.randomUUID();
    private final String blackDuckProjectName = System.getenv("TEST_BLACKDUCK_PROJECT_NAME");
    private final String blackDuckProjectVersion = System.getenv("TEST_BLACKDUCK_PROJECT_VERSION");

    private final String slackChannelKey = new SlackChannelKey().getUniversalKey();
    private final String slackChannelWebhook = System.getenv("TEST_SLACK_WEBHOOK");
    private final String slackChannelName = System.getenv("TEST_SLACK_CHANNEL_NAME");
    private final String slackChannelUsername = System.getenv("TEST_SLACK_CHANNEL_USERNAME");
    private final String slackJobProviderProjectPattern = System.getenv("TEST_PROVIDER_PROJECT_NAME_PATTERN");

    private final String alertURL = "https://localhost:8443/alert/";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);

    private BlackDuckServicesFactory blackDuckServicesFactory = null;
    private String blackDuckProviderID = "-1";

    @Test
    @Ignore
    public void testConfigureAlert() throws Exception {
        LocalTime startingTime = LocalTime.now();
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(startingTime)));

        // Create an authenticated connection to Alert
        loginToAlert();

        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalTime.now());
        startingTime = LocalTime.now();

        // Create the Black Duck Global provider configuration
        setupBlackDuck();

        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalTime.now());
        startingTime = LocalTime.now();

        int currentJobNum = 1;
        // create 10 slack jobs
        currentJobNum = createSlackJobs(startingTime, currentJobNum, 10, 10);
        intLogger.info("Current job number = " + currentJobNum);
        // trigger BD notification
        triggerBlackDuckNotification();

        // TODO check that all jobs have processed the notification, log how long it took

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

    @Test
    @Ignore
    public void testSandbox() throws Exception {
        triggerBlackDuckNotification();
    }

    private void triggerBlackDuckNotification() throws Exception {
        setupBlackDuckServicesFactory();
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectVersionWrapper projectVersion = projectService.getProjectVersion(blackDuckProjectName, blackDuckProjectVersion)
                                                   .orElseThrow(() -> new IntegrationException(String.format("Could not find the Black Duck project '%s' version '%s'", blackDuckProjectName, blackDuckProjectVersion)));

        ProjectVersionView projectVersionView = projectVersion.getProjectVersionView();
        List<ProjectVersionComponentView> bomComponents = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        Optional<ProjectVersionComponentView> apacheCommonsFileUpload = bomComponents.stream()
                                                                            .filter(component -> component.getComponentName().equals("Apache Commons FileUpload"))
                                                                            .filter(component -> component.getComponentVersionName().equals("1.2.1"))
                                                                            .findFirst();
        if (apacheCommonsFileUpload.isPresent()) {
            blackDuckService.delete(apacheCommonsFileUpload.get());
            //Thread.currentThread().wait(1000);
        }

        ExternalId commonsFileUploadExternalId = new ExternalId(Forge.MAVEN);
        commonsFileUploadExternalId.setGroup("commons-fileupload");
        commonsFileUploadExternalId.setName("commons-fileupload");
        commonsFileUploadExternalId.setVersion("1.2.1");

        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        projectBomService.addComponentToProjectVersion(commonsFileUploadExternalId, projectVersionView);
    }

    private void setupBlackDuckServicesFactory() {
        if (null == blackDuckServicesFactory) {
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            blackDuckServerConfigBuilder.setUrl(blackDuckProviderUrl)
                .setApiToken(blackDuckApiToken)
                .setTimeoutInSeconds(blackDuckTimeout)
                .setTrustCert(true);
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        }
    }

    private int createSlackJobs(LocalTime startingTime, int startingJobNum, int numberOfJobsToCreate, int intervalToLog) throws Exception {
        int jobNumber = startingJobNum;

        while (jobNumber <= startingJobNum + numberOfJobsToCreate) {

            // Create a Slack Job with a unique name using the job number
            createSlackJob(jobNumber);

            if (jobNumber % intervalToLog == 0) {
                String message = String.format("Creating %s jobs took", jobNumber);
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalTime.now());
            }
            jobNumber++;
        }
        return jobNumber;
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
        try {
            findBlackDuckProvider();
        } catch (IntegrationException exception) {
            intLogger.error(exception.getMessage());
            createBlackDuckProvider();
        }
    }

    private void findBlackDuckProvider() throws Exception {
        String blackDuckProviderSearch = String.format("api/configuration?context=%s&descriptorName=%s", ConfigContextEnum.GLOBAL, blackDuckProviderKey);
        Response response = executeGetRequest(blackDuckProviderSearch, "Could not find the Black Duck provider.");

        MultiFieldModel blackDuckConfigurations = gson.fromJson(response.getContentString(), MultiFieldModel.class);
        FieldModel blackDuckProviderConfiguration = blackDuckConfigurations.getFieldModels().stream()
                                                        .filter(blackDuckConfiguration -> blackDuckConfiguration.getFieldValue("blackduck.url").isPresent())
                                                        .filter(blackDuckConfiguration -> blackDuckConfiguration.getFieldValue("blackduck.url").get().equals(blackDuckProviderUrl))
                                                        .findFirst()
                                                        .orElseThrow(() -> new IntegrationException("Could not find the BlackDuck provider configuration."));

        blackDuckProviderID = blackDuckProviderConfiguration.getId();
        intLogger.info(String.format("Retrieved the Black Duck provider, ID %s.", blackDuckProviderID));
    }

    private void createBlackDuckProvider() throws Exception {
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

    private void createSlackJob(Integer jobNumber) throws Exception {
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

    private void logTimeElapsedWithMessage(String messageFormat, LocalTime start, LocalTime end) {
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

    private Response executeGetRequest(String path, String error) throws Exception {
        Request.Builder requestBuilder = createRequestBuilder(path);
        requestBuilder.method(HttpMethod.GET);
        Request request = requestBuilder.build();
        Response response = client.execute(request);
        if (response.isStatusCodeError()) {
            intLogger.error(error);
            response.throwExceptionForError();
        }
        return response;
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
