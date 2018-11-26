package com.synopsys.integration.alert.web.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.web.model.SystemSetupModel;
import com.synopsys.integration.alert.workflow.startup.install.RequiredSystemConfiguration;
import com.synopsys.integration.alert.workflow.startup.install.SystemInitializer;

public class SystemActionsTest {
    private SystemStatusUtility systemStatusUtility;
    private SystemMessageUtility systemMessageUtility;
    private SystemInitializer systemInitializer;

    @Before
    public void initiailize() {
        systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        systemInitializer = Mockito.mock(SystemInitializer.class);
        final List<SystemMessage> messages = createSystemMessageList();
        Mockito.when(systemMessageUtility.getSystemMessages()).thenReturn(messages);
        Mockito.when(systemMessageUtility.getSystemMessagesAfter(Mockito.any())).thenReturn(messages);
    }

    @Test
    public void getSystemMessagesSinceStartup() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        systemActions.getSystemMessagesSinceStartup();
        Mockito.verify(systemStatusUtility).getStartupTime();
        Mockito.verify(systemMessageUtility).getSystemMessagesAfter(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesAfter() throws Exception {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        systemActions.getSystemMessagesAfter("2018-11-13T00:00:00.000Z");
        Mockito.verify(systemMessageUtility).getSystemMessagesAfter(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesBefore() throws Exception {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        systemActions.getSystemMessagesBefore("2018-11-13T00:00:00.000Z");
        Mockito.verify(systemMessageUtility).getSystemMessagesBefore(Mockito.any());
    }

    @Test
    public void testGetSystemMessagesBetween() throws Exception {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        systemActions.getSystemMessagesBetween("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemMessageUtility).findBetween(Mockito.any());
    }

    @Test
    public void testGetSystemMessages() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        systemActions.getSystemMessages();
        Mockito.verify(systemMessageUtility).getSystemMessages();
    }

    private List<SystemMessage> createSystemMessageList() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime = zonedDateTime.minusMinutes(1);
        return Collections.singletonList(new SystemMessage(Date.from(zonedDateTime.toInstant()), "type", "content", "type"));
    }

    @Test
    public void testIsInitiailzed() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);

        assertFalse(systemActions.isSystemInitialized());
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(systemInitializer.isSystemInitialized()).thenReturn(Boolean.TRUE);
        assertTrue(systemActions.isSystemInitialized());
    }

    @Test
    public void testGetCurrentSystemSetup() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);
        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";

        final RequiredSystemConfiguration expected = new RequiredSystemConfiguration(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword);

        Mockito.when(systemInitializer.getCurrentSystemSetup()).thenReturn(expected);

        final SystemSetupModel actual = systemActions.getCurrentSystemSetup();

        assertEquals(blackDuckProviderUrl, actual.getBlackDuckProviderUrl());
        assertEquals(blackDuckConnectionTimeout, actual.getBlackDuckConnectionTimeout());
        assertNull(blackDuckApiToken, actual.getBlackDuckApiToken());
        assertTrue(actual.isBlackDuckApiTokenSet());
        assertNull(actual.getGlobalEncryptionPassword());
        assertTrue(actual.isGlobalEncryptionPasswordSet());
        assertNull(actual.getGlobalEncryptionSalt());
        assertTrue(actual.isGlobalEncryptionSaltSet());
        assertEquals(proxyHost, actual.getProxyHost());
        assertEquals(proxyPort, actual.getProxyPort());
        assertEquals(proxyUsername, actual.getProxyUsername());
        assertNull(actual.getProxyPassword());
        assertTrue(actual.isProxyPasswordSet());
    }

    @Test
    public void testSaveRequiredInformation() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility, systemInitializer);

        final String blackDuckProviderUrl = "url";
        final Integer blackDuckConnectionTimeout = 100;
        final String blackDuckApiToken = "token";
        final boolean blackDuckApiTokenSet = true;
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = true;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = true;
        final String proxyHost = "host";
        final String proxyPort = "port";
        final String proxyUsername = "username";
        final String proxyPassword = "password";
        final boolean proxyPasswordSet = true;

        final SystemSetupModel configuration = SystemSetupModel.of(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, blackDuckApiTokenSet,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword, proxyPasswordSet);

        final Map<String, String> fieldErrors = new HashMap<>();
        systemActions.saveRequiredInformation(configuration, fieldErrors);
        Mockito.verify(systemInitializer).updateRequiredConfiguration(Mockito.any(), Mockito.any());
        assertTrue(fieldErrors.isEmpty());
    }
}
