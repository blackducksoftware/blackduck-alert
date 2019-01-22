package com.synopsys.integration.alert.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.web.model.ResponseBodyBuilder;

@Component
public class ResponseFactory {

    public ResponseEntity<String> createResponse(final HttpStatus status, final String id, final String message) {
        final String responseBody = new ResponseBodyBuilder(id, message).build();
        return new ResponseEntity<>(responseBody, status);
    }

    public ResponseEntity<String> createResponse(final HttpStatus status, final Long id, final String message) {
        return createResponse(status, String.valueOf(id), message);
    }

    public ResponseEntity<String> createResponse(final HttpStatus status, final String message) {
        return createResponse(status, -1L, message);
    }

    public ResponseEntity<String> createNotFoundResponse(String message) {
        return createResponse(HttpStatus.NOT_FOUND, message);
    }

    public ResponseEntity<String> createCreatedResponse(String id, String message) {
        return createResponse(HttpStatus.CREATED, id, message);
    }

    public ResponseEntity<String> createAcceptedResponse(String id, String message) {
        return createResponse(HttpStatus.ACCEPTED, id, message);
    }

    public ResponseEntity<String> createOkResponse(String id, String message) {
        return createResponse(HttpStatus.OK, id, message);
    }

    public ResponseEntity<String> createMethodNotAllowedResponse() {
        return createResponse(HttpStatus.METHOD_NOT_ALLOWED, "This method is not allowed");
    }

    public ResponseEntity<String> createBadRequestResponse(String id, String message) {
        return createResponse(HttpStatus.BAD_REQUEST, id, message);
    }

    public ResponseEntity<String> createInternalServerErrorResponse(String id, String message) {
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, message);
    }

    public ResponseEntity<String> createConflictResponse(String id, String message) {
        return createResponse(HttpStatus.CONFLICT, id, message);
    }
}
