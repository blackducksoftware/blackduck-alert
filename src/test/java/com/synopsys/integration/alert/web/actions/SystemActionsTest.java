package com.synopsys.integration.alert.web.actions;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
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
}
