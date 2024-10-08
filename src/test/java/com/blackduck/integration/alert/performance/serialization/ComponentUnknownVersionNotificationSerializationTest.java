package com.blackduck.integration.alert.performance.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.Application;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.configuration.ApplicationConfiguration;
import com.blackduck.integration.alert.database.DatabaseDataSource;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.DescriptorMocker;
import com.blackduck.integration.bdio.model.Forge;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationView;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.wait.ResilientJobConfig;
import com.blackduck.integration.wait.WaitJob;
import com.blackduck.integration.wait.tracker.WaitIntervalTracker;
import com.blackduck.integration.wait.tracker.WaitIntervalTrackerFactory;
import com.google.gson.Gson;
import com.blackduck.integration.alert.performance.utility.AlertRequestUtility;
import com.blackduck.integration.alert.performance.utility.BlackDuckProviderService;
import com.blackduck.integration.alert.performance.utility.ConfigurationManagerLegacy;
import com.blackduck.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;

@Tag(TestTags.DEFAULT_INTEGRATION)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class ComponentUnknownVersionNotificationSerializationTest {
    private static final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(ComponentUnknownVersionNotificationSerializationTest.class));
    private final static String SLACK_JOB_NAME = "Slack Serialization Test Job";
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private NotificationAccessor notificationAccessor;

    private final Gson gson = IntegrationPerformanceTestRunnerLegacy.createGson();

    private static String SLACK_CHANNEL_WEBHOOK;
    private static String SLACK_CHANNEL_USERNAME;

    @BeforeAll
    public static void initTest() {
        TestProperties testProperties = new TestProperties();
        SLACK_CHANNEL_WEBHOOK = testProperties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK);
        SLACK_CHANNEL_USERNAME = testProperties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME);
    }

    @Test
    @Ignore // performance test
    @Disabled
    void testNotificationSerialization() throws IntegrationException {
        LocalDateTime searchStartTime = LocalDateTime.now().minusMinutes(1);
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunnerLegacy.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        configureJob(alertRequestUtility, blackDuckProviderService);

        ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.setGroup("commons-fileupload");
        externalId.setName("commons-fileupload");
        Predicate<ProjectVersionComponentVersionView> componentFilter = (component) -> component.getComponentName().equals("Apache Commons FileUpload");
        blackDuckProviderService.triggerBlackDuckNotification(() -> externalId, componentFilter);

        try {
            WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(300, 20);
            ResilientJobConfig resilientJobConfig = new ResilientJobConfig(
                intLogger,
                searchStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                waitIntervalTracker
            );
            NotificationReceivedWaitJobTask notificationWaitJobTask = new NotificationReceivedWaitJobTask(
                notificationAccessor,
                searchStartTime,
                "Apache Commons FileUpload",
                null,
                NotificationType.COMPONENT_UNKNOWN_VERSION
            );
            boolean isComplete = WaitJob.waitFor(resilientJobConfig, notificationWaitJobTask, "notification serialization test notification wait");

            if (isComplete) {
                String notificationContent = notificationWaitJobTask.getNotificationContent().orElseThrow(() -> new IllegalStateException("Expected notification is missing."));
                BlackDuckResponseResolver resolver = blackDuckProviderService.getBlackDuckServicesFactory().getBlackDuckResponseResolver();
                ComponentUnknownVersionNotificationView notificationView = resolver.resolve(notificationContent, ComponentUnknownVersionNotificationView.class);
                assertNotNull(notificationView.getContent());
                assertTrue(StringUtils.isNotBlank(notificationView.getContent().getComponentName()));

                BlackDuckApiClient apiClient = blackDuckProviderService.getBlackDuckServicesFactory().getBlackDuckApiClient();
                Optional<HttpUrl> componentUrl = HttpUrl.createSafely(notificationView.getContent().getBomComponent());
                if (componentUrl.isPresent()) {
                    apiClient.delete(componentUrl.get());
                }
            }
        } catch (InterruptedException ex) {
            // if a timeout happens that's ok we are trying to ensure deserialization is correct.
        }
    }

    private void configureJob(AlertRequestUtility alertRequestUtility, BlackDuckProviderService blackDuckProviderService) throws IntegrationException {
        String blackDuckProviderID = blackDuckProviderService.setupBlackDuck();
        ConfigurationManagerLegacy configurationManager = new ConfigurationManagerLegacy(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            ChannelKeys.SLACK.getUniversalKey()
        );

        Map<String, FieldValueModel> slackJobFields = new HashMap<>();
        slackJobFields.put(ChannelDescriptor.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        slackJobFields.put(ChannelDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(ChannelKeys.SLACK.getUniversalKey()), true));
        slackJobFields.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(SLACK_JOB_NAME), true));
        slackJobFields.put(ChannelDescriptor.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackJobFields.put(ChannelDescriptor.KEY_PROVIDER_TYPE, new FieldValueModel(List.of(blackDuckProviderService.getBlackDuckProviderKey()), true));

        slackJobFields.put(SlackDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(SLACK_CHANNEL_WEBHOOK), true));
        slackJobFields.put(SlackDescriptor.KEY_CHANNEL_USERNAME, new FieldValueModel(List.of(SLACK_CHANNEL_USERNAME), true));

        configurationManager.createJob(
            slackJobFields,
            SLACK_JOB_NAME,
            blackDuckProviderID,
            blackDuckProviderService.getBlackDuckProjectName(),
            List.of(NotificationType.COMPONENT_UNKNOWN_VERSION)
        );
    }
}
