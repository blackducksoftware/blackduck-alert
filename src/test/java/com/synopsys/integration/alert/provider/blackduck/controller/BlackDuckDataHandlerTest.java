package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckDataHandlerTest {
    private final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

    @Test
    public void testGetHubGroups() throws Exception {

        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckGroups()).thenReturn(Collections.emptyList());
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckGroups();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"[]\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowIntegrationRestException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckGroups()).thenThrow(new IntegrationRestException(402, "StatusMessage", "ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckGroups();
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"StatusMessage : ErrorMessage: 402: StatusMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowIntegrationException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckGroups()).thenThrow(new IntegrationException("ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckGroups();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckGroups()).thenThrow(new IllegalStateException("ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckGroups();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenReturn(Collections.emptyList());
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckProjects();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"[]\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowIntegrationRestException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenThrow(new IntegrationRestException(402, "StatusMessage", "ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckProjects();
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"StatusMessage : ErrorMessage: 402: StatusMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowIntegrationException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenThrow(new IntegrationException("ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckProjects();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenThrow(new IllegalStateException("ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckProjects();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }
}
