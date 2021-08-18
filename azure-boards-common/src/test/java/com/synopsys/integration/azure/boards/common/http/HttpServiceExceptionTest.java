package com.synopsys.integration.azure.boards.common.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HttpServiceExceptionTest {
    @Test
    void internalServerErrorTest() {
        String testMessage = "error!";
        HttpServiceException httpServiceException = HttpServiceException.internalServerError(testMessage);
        assertException(httpServiceException, 500, testMessage);

        Exception exception = new Exception(testMessage);
        HttpServiceException httpServiceExceptionFromThrowable = HttpServiceException.internalServerError(exception);
        assertException(httpServiceExceptionFromThrowable, 500, testMessage);
    }

    @Test
    void forbiddenError() {
        String testMessage = "forbidden!";
        HttpServiceException httpServiceException = HttpServiceException.forbiddenError(testMessage);
        assertException(httpServiceException, 403, testMessage);

        Exception exception = new Exception("uh oh");
        HttpServiceException httpServiceExceptionFromThrowable = HttpServiceException.forbiddenError(testMessage, exception);
        assertException(httpServiceExceptionFromThrowable, 403, testMessage);
    }

    @Test
    void notFoundError() {
        String testMessage = "not found!";
        HttpServiceException httpServiceException = HttpServiceException.notFoundError(testMessage);
        assertException(httpServiceException, 404, testMessage);
    }

    @Test
    void errorCodeConstructorTest() {
        int errorCode = 401;
        HttpServiceException httpServiceException = new HttpServiceException(errorCode);
        assertEquals(errorCode, httpServiceException.getHttpErrorCode());
    }

    private static void assertException(HttpServiceException e, int expectedCode, String expectedMessage) {
        assertEquals(expectedCode, e.getHttpErrorCode());
        assertTrue(e.getMessage().contains(expectedMessage), "Exception did not contain expected message");
    }

}
