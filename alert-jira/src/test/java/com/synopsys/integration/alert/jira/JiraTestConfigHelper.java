package com.synopsys.integration.alert.jira;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.synopsys.integration.alert.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class JiraTestConfigHelper {
    public static final String ENV_BASE_URL = "JIRA_CLOUD_URL";
    public static final String ENV_USER_EMAIL = "JIRA_CLOUD_EMAIL";
    public static final String ENV_API_TOKEN = "JIRA_CLOUD_TOKEN";
    public static final String TEST_PROJECT = "JIRA_CLOUD_TEST_PROJECT";
    public static final String TEST_PROPERTY_KEY = "custom.synopsys.test.property.key";

    public void validateConfiguration() {
        String baseUrl = getEnvBaseUrl();
        String userEmail = getEnvUserEmail();
        String apiToken = getEnvApiToken();
        String testProject = getTestProject();

        assumeTrue(null != baseUrl, "No Jira Cloud base url provided");
        assumeTrue(null != userEmail, "No Jira Cloud user email provided");
        assumeTrue(null != apiToken, "No Jira Cloud API Token provided");
        assumeTrue(null != testProject, "No Jira Cloud Test project provided");
    }

    public JiraCloudProperties createJiraCloudProperties() {
        return new JiraCloudProperties(getEnvBaseUrl(), getEnvApiToken(), getEnvUserEmail());
    }

    public JiraCloudRestConfig createJiraServerConfig() {
        JiraCloudRestConfigBuilder builder = JiraCloudRestConfig.newBuilder();

        builder.setUrl(getEnvBaseUrl())
            .setAuthUserEmail(getEnvUserEmail())
            .setApiToken(getEnvApiToken());
        return builder.build();
    }

    public JiraCloudServiceFactory createServiceFactory() {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.WARN);
        JiraCloudRestConfig serverConfig = createJiraServerConfig();
        return serverConfig.createJiraCloudServiceFactory(logger);
    }

    public String getEnvBaseUrl() {
        return System.getenv(ENV_BASE_URL);
    }

    public String getEnvUserEmail() {
        return System.getenv(ENV_USER_EMAIL);
    }

    public String getEnvApiToken() {
        return System.getenv(ENV_API_TOKEN);
    }

    public String getTestProject() {
        return System.getenv(TEST_PROJECT);
    }
}
