package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class JobFieldErrorsTest {
    @Test
    public void getFieldErrorsTest() {
        String fieldErrorKey = "key";
        String fieldErrorValue = "value";

        Map<String, AlertFieldStatus> fieldError = new HashMap<>();
        fieldError.put(fieldErrorKey, AlertFieldStatus.error(fieldErrorValue));
        Map<String, AlertFieldStatus> testResult = new JobFieldErrors(fieldError).getFieldErrors();

        assertEquals(fieldError, testResult);
    }

    @Test
    public void getFieldErrorsWithIdTest() {
        String fieldErrorKey = "key";
        String fieldErrorValue = "value";
        String configId = "testID";
        String newConfigId = "newTestID";

        Map<String, AlertFieldStatus> fieldError = new HashMap<>();
        fieldError.put(fieldErrorKey, AlertFieldStatus.error(fieldErrorValue));
        JobFieldErrors jobFieldError = new JobFieldErrors(configId, fieldError);
        Map<String, AlertFieldStatus> testResult = jobFieldError.getFieldErrors();

        assertEquals(testResult, fieldError);
        assertEquals(configId, jobFieldError.getId());

        jobFieldError.setId(newConfigId);

        assertEquals(newConfigId, jobFieldError.getId());
    }
}
