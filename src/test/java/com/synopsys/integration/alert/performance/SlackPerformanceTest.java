package com.synopsys.integration.alert.performance;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerLegacy;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class SlackPerformanceTest {
    private final static String SLACK_PERFORMANCE_JOB_NAME = "Slack Performance Job";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Gson gson = IntegrationPerformanceTestRunnerLegacy.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerLegacy.createDateTimeFormatter();

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
    void testSlackJob() throws Exception {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunnerLegacy.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerLegacy configurationManager = new ConfigurationManagerLegacy(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            ChannelKeys.SLACK.getUniversalKey()
        );
        IntegrationPerformanceTestRunnerLegacy integrationPerformanceTestRunner = new IntegrationPerformanceTestRunnerLegacy(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );

        Map<String, FieldValueModel> slackJobFields = new HashMap<>();
        slackJobFields.put(ChannelDescriptor.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        slackJobFields.put(ChannelDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(ChannelKeys.SLACK.getUniversalKey()), true));
        slackJobFields.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(SLACK_PERFORMANCE_JOB_NAME), true));
        slackJobFields.put(ChannelDescriptor.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        slackJobFields.put(ChannelDescriptor.KEY_PROVIDER_TYPE, new FieldValueModel(List.of(blackDuckProviderService.getBlackDuckProviderKey()), true));

        slackJobFields.put(SlackDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(SLACK_CHANNEL_WEBHOOK), true));
        slackJobFields.put(SlackDescriptor.KEY_CHANNEL_USERNAME, new FieldValueModel(List.of(SLACK_CHANNEL_USERNAME), true));

        integrationPerformanceTestRunner.runTest(slackJobFields, SLACK_PERFORMANCE_JOB_NAME);
    }

}
