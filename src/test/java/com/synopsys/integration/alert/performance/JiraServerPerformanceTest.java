package com.synopsys.integration.alert.performance;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerV2;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerV2;
import com.synopsys.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class JiraServerPerformanceTest {
    private static final String PERFORMANCE_JOB_NAME = "Jira Server Performance Job";
    private static final String DEFAULT_JOB_NAME = "JiraPerformanceJob";
    private static final JiraServerChannelKey CHANNEL_KEY = new JiraServerChannelKey();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final Gson gson = IntegrationPerformanceTestRunner.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerV2.createDateTimeFormatter();

    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunnerV2 testRunner;

    @BeforeEach
    public void init() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunnerV2.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerV2 configurationManager = new ConfigurationManagerV2(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
        jiraServerPerformanceUtility = new JiraServerPerformanceUtility(alertRequestUtility, configurationManager);
        testRunner = new IntegrationPerformanceTestRunnerV2(
            gson,
            dateTimeFormatter,
            alertRequestUtility,
            blackDuckProviderService,
            configurationManager,
            14400
        );
    }

    @Test
    @Disabled("Used for performance testing only.")
    void jiraServerJobTest() throws Exception {
        TestProperties testProperties = new TestProperties();
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = jiraServerPerformanceUtility.createGlobalConfigModelFromProperties(testProperties);

        JiraServerGlobalConfigModel globalConfiguration = jiraServerPerformanceUtility.createJiraGlobalConfiguration(jiraServerGlobalConfigModel);

        Map<String, FieldValueModel> channelFieldsMap = jiraServerPerformanceUtility.createChannelFieldsMap(testProperties, DEFAULT_JOB_NAME, globalConfiguration.getId());

        testRunner.runTest(channelFieldsMap, PERFORMANCE_JOB_NAME);
    }
}
