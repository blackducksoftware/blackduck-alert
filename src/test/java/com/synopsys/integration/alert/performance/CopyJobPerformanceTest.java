package com.synopsys.integration.alert.performance;

import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.configuration.ApplicationConfiguration;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.performance.utility.BlackDuckProviderService;
import com.synopsys.integration.alert.performance.utility.ConfigurationManagerLegacy;
import com.synopsys.integration.alert.performance.utility.ExternalAlertRequestUtility;
import com.synopsys.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@SpringBootTest
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
class CopyJobPerformanceTest {
    private final Gson gson = IntegrationPerformanceTestRunnerLegacy.createGson();
    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));
    private final IntHttpClient client = new IntHttpClient(intLogger, gson, 60, true, ProxyInfo.NO_PROXY_INFO);
    private final String alertURL = "https://localhost:8443/alert";

    @Test
    @Ignore // performance test
    @Disabled
    void copyJobTest() throws IntegrationException {
        ExternalAlertRequestUtility alertRequestUtility = new ExternalAlertRequestUtility(intLogger, client, alertURL);
        // Create an authenticated connection to Alert
        alertRequestUtility.loginToExternalAlert();
        BlackDuckProviderService blackDuckProviderService = new BlackDuckProviderService(alertRequestUtility, gson);
        ConfigurationManagerLegacy configurationManager = new ConfigurationManagerLegacy(
            gson,
            alertRequestUtility,
            blackDuckProviderService.getBlackDuckProviderKey(),
            ChannelKeys.SLACK.getUniversalKey()
        );
        int count = 1000;
        String jobName = URLEncoder.encode("Jira Server", Charset.defaultCharset());
        for (int index = 1; index <= count; index++) {
            String newJobName = String.format("%s_%d", jobName, index);
            configurationManager.copyJob(jobName, newJobName);
        }
    }

}
