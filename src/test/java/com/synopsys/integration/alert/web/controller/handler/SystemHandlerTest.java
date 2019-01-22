package com.synopsys.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.web.actions.SystemActions;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

public class SystemHandlerTest {
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
    private SystemActions systemActions;

    @BeforeEach
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
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);

        final String contextPath = "context-path/";
        final ResponseEntity<String> response = handler.getInitialSetup(contextPath);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).getCurrentSystemSetup();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        final String body = response.getBody();
        assertNull(body);
    }

    @Test
    public void testGetCurrentSetupInitialized() {
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.TRUE);
        final String contextPath = "context-path/";
        final ResponseEntity<String> response = handler.getInitialSetup(contextPath);
        Mockito.verify(systemActions).isSystemInitialized();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(contextPath, response.getHeaders().getFirst("Location"));
    }

    @Test
    public void testSaveNotAllowed() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.TRUE);
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> response = handler.saveInitialSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSaveWithErrors() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.FALSE);
        Mockito.doAnswer(invocation -> {
            final Map<String, String> fieldErrors = invocation.getArgument(1);
            fieldErrors.put("propertyKey", "error");
            return invocation.getArgument(0);
        }).when(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> response = handler.saveInitialSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testSave() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.FALSE);
        final SystemHandler handler = new SystemHandler(contentConverter, systemActions);
        final ResponseEntity<String> response = handler.saveInitialSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
