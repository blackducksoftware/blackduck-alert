package com.synopsys.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.install.SystemInitializer;
import com.synopsys.integration.alert.web.actions.SystemActions;

public class SystemHandlerTest {
    private SystemActions systemActions;
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);

    @Before
    public void initialize() {
        systemActions = Mockito.spy(new SystemActions(Mockito.mock(SystemStatusUtility.class), Mockito.mock(SystemMessageUtility.class), Mockito.mock(SystemInitializer.class)));
    }

    @Test
    public void testGetLatestMessages() {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getLatestMessagesSinceStartup();
        Mockito.verify(systemActions).getSystemMessagesSinceStartup();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesgetAll() {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("", "");
        Mockito.verify(systemActions).getSystemMessages();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetAfter() throws Exception {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", null);
        Mockito.verify(systemActions).getSystemMessagesAfter(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBefore() throws Exception {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages(null, "2018-11-13T00:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBefore(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBetween() throws Exception {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesBadDateRange() throws Exception {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("bad-start-time", "bad-end-time");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

}
