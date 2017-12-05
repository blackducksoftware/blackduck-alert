package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class ControllerHandlerTest {
    @Test
    public void testCreateResponse() {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(objectTransformer);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"Test\"}", responseEntity.getBody());
    }

    @Test
    public void testCreateResponseWithStringID() {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(objectTransformer);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, "11", "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":11,\"message\":\"Test\"}", responseEntity.getBody());
    }

    @Test
    public void testCreateResponseWithLongID() {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(objectTransformer);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, 21L, "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":21,\"message\":\"Test\"}", responseEntity.getBody());
    }

    private static class ControllerHandlerTestObject extends ControllerHandler {
        public ControllerHandlerTestObject(final ObjectTransformer objectTransformer) {
            super(objectTransformer);
        }
    }
}
