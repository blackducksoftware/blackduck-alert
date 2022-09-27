package com.synopsys.integration.alert.api.channel.jira.util;

import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class JiraPluginCheckUtilsTest {
    @Mock
    private PluginManagerService mockPluginManagerService;

    @Test
    void checkReturnsTrueIfAppInstalled() throws IntegrationException, InterruptedException {
        Mockito.when(mockPluginManagerService.isAppInstalled(JiraConstants.JIRA_APP_KEY)).thenReturn(true);

        assertTrue(JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(mockPluginManagerService));
        // Called once only if already installed
        verify(mockPluginManagerService, times(1)).isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Test
    void checkReturnsTrueOnNthAttempt() throws IntegrationException, InterruptedException {
        Mockito.when(mockPluginManagerService.isAppInstalled(JiraConstants.JIRA_APP_KEY)).thenReturn(false, false, true);

        assertTrue(JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(mockPluginManagerService, 0L));
        // Called with 3rd attempt returning true
        verify(mockPluginManagerService, times(3)).isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }

    @Test
    void checkReturnsFalseIfAppNotInstalled() throws IntegrationException, InterruptedException {
        Mockito.when(mockPluginManagerService.isAppInstalled(JiraConstants.JIRA_APP_KEY)).thenReturn(false);

        assertFalse(JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(mockPluginManagerService, 0L));
        // Called max number times for not installed
        verify(mockPluginManagerService, times((int) JiraPluginCheckUtils.MAX_NUMBER_OF_RETRIES)).isAppInstalled(JiraConstants.JIRA_APP_KEY);
    }
}
