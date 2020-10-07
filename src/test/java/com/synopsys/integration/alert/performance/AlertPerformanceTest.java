package com.synopsys.integration.alert.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJob;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public class AlertPerformanceTest {
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
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final IntHttpClient client = new IntHttpClient(intLogger, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final AlertRequestUtility alertRequestUtility = new AlertRequestUtility(intLogger, client, alertURL);

    private BlackDuckServicesFactory blackDuckServicesFactory = null;
    private String blackDuckProviderID = "-1";

    @Test
    @Ignore
    public void testAlertPerformance() throws Exception {
        LocalDateTime startingTime = LocalDateTime.now();
        intLogger.info(String.format("Starting time %s", dateTimeFormatter.format(startingTime)));

        // Create an authenticated connection to Alert
        loginToAlert();

        logTimeElapsedWithMessage("Logging in took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        // Create the Black Duck Global provider configuration
        setupBlackDuck();

        logTimeElapsedWithMessage("Configuring the Black Duck provider took %s", startingTime, LocalDateTime.now());
        startingTime = LocalDateTime.now();

        int currentJobNum = 0;
        // create 10 slack jobs
        currentJobNum = createSlackJobs(startingTime, currentJobNum, 10, 10);

        LocalDateTime startingSearchDateTime = LocalDateTime.now();
        // trigger BD notification
        triggerBlackDuckNotification();

        NotificationWaitJobTask notificationWaitJobTask = new NotificationWaitJobTask(intLogger, dateTimeFormatter, gson, alertRequestUtility, startingSearchDateTime, 10);
        WaitJob waitForNotificationToBeProcessed = WaitJob.create(intLogger, 600, startingSearchDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 20, notificationWaitJobTask);
        boolean isComplete = waitForNotificationToBeProcessed.waitFor();

        intLogger.info("Finished waiting for the notification to be processed: " + isComplete);

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
        // trigger BD notification
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

    private int createSlackJobs(LocalDateTime startingTime, int startingJobNum, int numberOfJobsToCreate, int intervalToLog) throws Exception {
        int jobNumber = startingJobNum;

        while (jobNumber < startingJobNum + numberOfJobsToCreate) {
            jobNumber++;
            // Create a Slack Job with a unique name using the job number
            createSlackJob(jobNumber);

            if (jobNumber % intervalToLog == 0) {
                String message = String.format("Creating %s jobs took", jobNumber);
                logTimeElapsedWithMessage(message + " %s", startingTime, LocalDateTime.now());
            }
        }
        intLogger.info(String.format("Finished creating %s jobs. Current Job number %s.", numberOfJobsToCreate, jobNumber));
        return jobNumber;
    }

    private void loginToAlert() throws Exception {
        String loginBody = "{\"alertUsername\":\"sysadmin\",\"alertPassword\":\"blackduck\"}";
        BodyContent requestBody = new StringBodyContent(loginBody);
        Response response = alertRequestUtility.executePostRequest("api/login", requestBody, "Could not log into Alert.");

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
        Response response = alertRequestUtility.executeGetRequest(blackDuckProviderSearch, "Could not find the Black Duck provider.");

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

        alertRequestUtility.executePostRequest("api/configuration/validate", requestBody, "Validating the Black Duck provider failed.");
        alertRequestUtility.executePostRequest("api/configuration/test", requestBody, "Testing the Black Duck provider failed.");
        Response creationResponse = alertRequestUtility.executePostRequest("api/configuration", requestBody, "Could not create the Black Duck provider.");

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

        alertRequestUtility.executePostRequest("api/configuration/job/validate", requestBody, String.format("Validating the Slack Job #%s failed.", jobNumber));
        // executePostRequest("api/configuration/job/test", requestBody, String.format("Testing the Slack Job #%s failed.", jobNumber));
        alertRequestUtility.executePostRequest("api/configuration/job", requestBody, String.format("Could not create the Slack Job #%s.", jobNumber));
    }

    private void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

}
