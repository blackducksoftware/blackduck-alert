package com.synopsys.integration.alert.web.actions;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;

public class SystemActionsTest {
    private final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
    private final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);

    @Test
    public void testGetLatestSystemMessages() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility);
        systemActions.getLatestSystemMessages();
        Mockito.verify(systemStatusUtility).getStartupTime();
        Mockito.verify(systemMessageUtility).getSystemMessagesSince(Mockito.any());
    }

    @Test
    public void testGetSystemMessages() {
        final SystemActions systemActions = new SystemActions(systemStatusUtility, systemMessageUtility);
        systemActions.getSystemMessages();
        Mockito.verify(systemMessageUtility).getSystemMessages();
    }
}
