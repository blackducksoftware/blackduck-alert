package com.blackducksoftware.integration.hub.alert.hub.controller;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

public class HubDataHandlerTest {

    @Test
    public void testGetHubGroups() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubGroups()).thenReturn(Collections.emptyList());
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubGroups();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"[]\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowIntegrationRestException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubGroups()).thenThrow(new IntegrationRestException(402, "StatusMessage", "ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubGroups();
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"StatusMessage : ErrorMessage: 402: StatusMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowIntegrationException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubGroups()).thenThrow(new IntegrationException("ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubGroups();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubGroupsThrowException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubGroups()).thenThrow(new IllegalStateException("ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubGroups();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubProjects()).thenReturn(Collections.emptyList());
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubProjects();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"[]\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowIntegrationRestException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubProjects()).thenThrow(new IntegrationRestException(402, "StatusMessage", "ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubProjects();
        assertEquals(HttpStatus.PAYMENT_REQUIRED, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"StatusMessage : ErrorMessage: 402: StatusMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowIntegrationException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubProjects()).thenThrow(new IntegrationException("ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubProjects();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowException() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final Gson gson = new Gson();
        final HubDataActions hubDataActions = Mockito.mock(HubDataActions.class);
        Mockito.when(hubDataActions.getHubProjects()).thenThrow(new IllegalStateException("ErrorMessage"));
        final HubDataHandler hubDataHandler = new HubDataHandler(objectTransformer, gson, hubDataActions);
        final ResponseEntity<String> responseEntity = hubDataHandler.getHubProjects();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }
}
