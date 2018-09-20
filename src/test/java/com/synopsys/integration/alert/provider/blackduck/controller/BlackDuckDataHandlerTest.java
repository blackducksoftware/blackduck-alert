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

public class BlackDuckDataHandlerTest {
    private final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

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
    public void testGetHubProjectsThrowException() throws Exception {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenThrow(new IllegalStateException("ErrorMessage"));
        final BlackDuckDataHandler blackDuckDataHandler = new BlackDuckDataHandler(contentConverter, blackDuckDataActions);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getBlackDuckProjects();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }
}
