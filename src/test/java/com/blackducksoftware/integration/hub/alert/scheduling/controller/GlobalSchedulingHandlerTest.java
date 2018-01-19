package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;

public class GlobalSchedulingHandlerTest {

    @Test
    public void testRunAccumulator() {
        final GlobalSchedulingConfigActions actions = Mockito.mock(GlobalSchedulingConfigActions.class);
        final GlobalSchedulingHandler handler = new GlobalSchedulingHandler(GlobalSchedulingConfigEntity.class, GlobalSchedulingConfigRestModel.class, actions, null);
        final ResponseEntity<String> response = handler.runAccumulator();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testRunAccumulatorWithException() throws Exception {
        final GlobalSchedulingConfigActions actions = Mockito.mock(GlobalSchedulingConfigActions.class);
        Mockito.doThrow(new IntegrationException("Test Exception")).when(actions).runAccumulator();
        final GlobalSchedulingHandler handler = new GlobalSchedulingHandler(GlobalSchedulingConfigEntity.class, GlobalSchedulingConfigRestModel.class, actions, null);
        final ResponseEntity<String> response = handler.runAccumulator();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
