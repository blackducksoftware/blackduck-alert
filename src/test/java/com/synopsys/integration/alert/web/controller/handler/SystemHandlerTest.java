package com.synopsys.integration.alert.web.controller.handler;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.SystemActions;

public class SystemHandlerTest {
    private final SystemActions systemActions = Mockito.mock(SystemActions.class);
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);

    @Test
    public void testGetLatestMessages() {
        SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        handler.getLatestMessages();
        Mockito.verify(systemActions).getLatestSystemMessages();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
    }

    @Test
    public void testGetSystemMessages() {
        SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        handler.getSystemMessages();
        Mockito.verify(systemActions).getSystemMessages();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
    }
}
