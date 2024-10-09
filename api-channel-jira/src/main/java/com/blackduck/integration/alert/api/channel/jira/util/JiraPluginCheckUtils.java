package com.blackduck.integration.alert.api.channel.jira.util;

import java.util.concurrent.TimeUnit;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;

public final class JiraPluginCheckUtils {
    public static final long MAX_NUMBER_OF_RETRIES = 5L;
    public static final long RETRY_SLEEP_TIME = 3L;

    public static boolean checkIsAppInstalledAndRetryIfNecessary(PluginManagerService pluginManagerService) throws IntegrationException, InterruptedException {
        return checkIsAppInstalledAndRetryIfNecessary(pluginManagerService, RETRY_SLEEP_TIME);
    }

    public static boolean checkIsAppInstalledAndRetryIfNecessary(PluginManagerService pluginManagerService, long retrySleepTime) throws IntegrationException, InterruptedException {
        long checkAgain = 1L;
        while (checkAgain <= MAX_NUMBER_OF_RETRIES) {
            boolean isAppInstalled = pluginManagerService.isAppInstalled(JiraConstants.JIRA_APP_KEY);
            if (isAppInstalled) {
                return true;
            }
            TimeUnit.SECONDS.sleep(retrySleepTime);
            checkAgain++;
        }
        return false;
    }

    private JiraPluginCheckUtils() {
        // This class should not be instantiated
    }

}
