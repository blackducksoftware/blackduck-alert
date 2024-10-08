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

import com.blackduck.integration.alert.Application;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.configuration.ApplicationConfiguration;
import com.blackduck.integration.alert.database.DatabaseDataSource;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestTags;
import com.google.gson.Gson;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManager;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunner;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;
import com.synopsys.integration.alert.performance.utility.jira.server.JiraServerPerformanceUtility;
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

    private final Gson gson = IntegrationPerformanceTestRunnerLegacy.createGson();
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunner.createDateTimeFormatter();

    private JiraServerPerformanceUtility jiraServerPerformanceUtility;
    private IntegrationPerformanceTestRunner testRunner;

    @BeforeEach
    public void init() {
        AlertRequestUtility alertRequestUtility = IntegrationPerformanceTestRunner.createAlertRequestUtility(webApplicationContext);
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManager configurationManager = new ConfigurationManager(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            CHANNEL_KEY.getUniversalKey()
        );
        jiraServerPerformanceUtility = new JiraServerPerformanceUtility(alertRequestUtility, configurationManager);
        testRunner = new IntegrationPerformanceTestRunner(
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
