package com.blackducksoftware.integration.hub.alert.web.controller.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ResponseBodyBuilder;

public abstract class ControllerHandler {
    private final ObjectTransformer objectTransformer;

    public ControllerHandler(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String id, final String message) {
        return createResponse(status, objectTransformer.stringToLong(id), message);
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

    protected ResponseEntity<String> createResponse(final HttpStatus status, final String message) {
        return createResponse(status, -1L, message);
    }

}
