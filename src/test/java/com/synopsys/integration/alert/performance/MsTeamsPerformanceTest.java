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

import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerLegacy;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class MsTeamsPerformanceTest {
    private final static String MS_TEAMS_PERFORMANCE_JOB_NAME = "MsTeams Performance Job";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Gson gson = IntegrationPerformanceTestRunnerLegacy.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerLegacy.createDateTimeFormatter();

    private static String MSTEAMS_CHANNEL_WEBHOOK;

    @BeforeAll
    public static void initTest() {
        TestProperties testProperties = new TestProperties();
        MSTEAMS_CHANNEL_WEBHOOK = testProperties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK);
    }

    @Test
    @Ignore // performance test
    @Disabled
    void testMsTeamsJob() throws Exception {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunnerLegacy.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerLegacy configurationManager = new ConfigurationManagerLegacy(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            ChannelKeys.MS_TEAMS.getUniversalKey()
        );
        IntegrationPerformanceTestRunnerLegacy integrationPerformanceTestRunner = new IntegrationPerformanceTestRunnerLegacy(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager
        );

        Map<String, FieldValueModel> msTeamsJobFields = new HashMap<>();
        msTeamsJobFields.put(ChannelDescriptor.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        msTeamsJobFields.put(ChannelDescriptor.KEY_CHANNEL_NAME, new FieldValueModel(List.of(ChannelKeys.MS_TEAMS.getUniversalKey()), true));
        msTeamsJobFields.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(MS_TEAMS_PERFORMANCE_JOB_NAME), true));
        msTeamsJobFields.put(ChannelDescriptor.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        msTeamsJobFields.put(ChannelDescriptor.KEY_PROVIDER_TYPE, new FieldValueModel(List.of(blackDuckProviderService.getBlackDuckProviderKey()), true));
        msTeamsJobFields.put(MsTeamsDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(MSTEAMS_CHANNEL_WEBHOOK), true));

        integrationPerformanceTestRunner.runTest(msTeamsJobFields, MS_TEAMS_PERFORMANCE_JOB_NAME);
    }

}
