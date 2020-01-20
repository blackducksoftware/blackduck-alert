package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class JobFieldErrorsTest {
    @Test
    public void getFieldErrorsTest() {
        final String fieldErrorKey = "key";
        final String fieldErrorValue = "value";

        Map<String, String> fieldError = new HashMap<>();
        fieldError.put(fieldErrorKey, fieldErrorValue);
        Map<String, String> testResult = new JobFieldErrors(fieldError).getFieldErrors();

        assertEquals(fieldError, testResult);
    }

    @Test
    public void getFieldErrorsWithIdTest() {
        final String fieldErrorKey = "key";
        final String fieldErrorValue = "value";
        final String configId = "testID";
        final String newConfigId = "newTestID";

        Map<String, String> fieldError = new HashMap<>();
        fieldError.put(fieldErrorKey, fieldErrorValue);

        JobFieldErrors jobFieldError = new JobFieldErrors(configId, fieldError);
        Map<String, String> testResult = jobFieldError.getFieldErrors();

        assertEquals(testResult, fieldError);
        assertEquals(configId, jobFieldError.getId());

        jobFieldError.setId(newConfigId);

        assertEquals(newConfigId, jobFieldError.getId());
    }
}
