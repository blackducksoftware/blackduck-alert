/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.util;

import java.util.concurrent.TimeUnit;

import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

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
