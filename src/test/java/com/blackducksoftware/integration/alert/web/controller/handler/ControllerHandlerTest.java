package com.blackducksoftware.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.google.gson.Gson;

public class ControllerHandlerTest {
    @Test
    public void testCreateResponse() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(contentConverter);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":-1,\"message\":\"Test\"}", responseEntity.getBody());
    }

    @Test
    public void testCreateResponseWithStringID() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(contentConverter);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, "11", "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":11,\"message\":\"Test\"}", responseEntity.getBody());
    }

    @Test
    public void testCreateResponseWithLongID() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ControllerHandlerTestObject controllerHandlerTestObject = new ControllerHandlerTestObject(contentConverter);

        final ResponseEntity<String> responseEntity = controllerHandlerTestObject.createResponse(HttpStatus.CHECKPOINT, 21L, "Test");
        assertEquals(HttpStatus.CHECKPOINT, responseEntity.getStatusCode());
        assertEquals("{\"id\":21,\"message\":\"Test\"}", responseEntity.getBody());
    }

    private static class ControllerHandlerTestObject extends ControllerHandler {
        public ControllerHandlerTestObject(final ContentConverter contentConverter) {
            super(contentConverter);
        }
    }
}
