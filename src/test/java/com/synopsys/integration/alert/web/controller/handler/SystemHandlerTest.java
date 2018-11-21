package com.synopsys.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.SystemActions;
import com.synopsys.integration.alert.web.model.SystemSetupModel;

public class SystemHandlerTest {
    private SystemActions systemActions;
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);

    @Before
    public void initialize() {
        systemActions = Mockito.mock(SystemActions.class);
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
        Mockito.when(systemActions.getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString())).thenThrow(new ParseException("errorparsing date ", 0));
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("bad-start-time", "bad-end-time");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testGetCurrentSetup() {
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

        final SystemSetupModel model = new SystemSetupModel(blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, blackDuckApiTokenSet,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword, proxyPasswordSet);

        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);

        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.FALSE);
        Mockito.when(systemActions.getCurrentSystemSetup()).thenReturn(model);
        final String contextPath = "context-path/";
        final ResponseEntity<String> response = handler.getCurrentSetup(contextPath);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).getCurrentSystemSetup();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        final String body = response.getBody();
        assertNotNull(body);
    }

    @Test
    public void testGetCurrentSetupInitialized() {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.TRUE);
        final String contextPath = "context-path/";
        final ResponseEntity<String> response = handler.getCurrentSetup(contextPath);
        Mockito.verify(systemActions).isSystemInitialized();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(contextPath, response.getHeaders().getFirst("Location"));
    }

}
