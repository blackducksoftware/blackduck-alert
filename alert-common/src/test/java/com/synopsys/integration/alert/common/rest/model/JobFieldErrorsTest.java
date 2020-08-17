package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class JobFieldErrorsTest {
    @Test
    public void getFieldErrorsTest() {
        String fieldErrorKey = "key";
        String fieldErrorValue = "value";

        List<AlertFieldStatus> fieldError = new ArrayList<>();
        fieldError.add(AlertFieldStatus.error(fieldErrorKey, fieldErrorValue));
        List<AlertFieldStatus> testResult = new JobFieldStatuses(fieldError).getFieldStatuses();

        assertEquals(fieldError, testResult);
    }

    @Test
    public void getFieldErrorsWithIdTest() {
        String fieldErrorKey = "key";
        String fieldErrorValue = "value";
        String configId = "testID";
        String newConfigId = "newTestID";

        List<AlertFieldStatus> fieldError = new ArrayList<>();
        fieldError.add(AlertFieldStatus.error(fieldErrorKey, fieldErrorValue));
        JobFieldStatuses jobFieldError = new JobFieldStatuses(configId, fieldError);
        List<AlertFieldStatus> testResult = jobFieldError.getFieldStatuses();

        assertEquals(testResult, fieldError);
        assertEquals(configId, jobFieldError.getId());

        jobFieldError.setId(newConfigId);

        assertEquals(newConfigId, jobFieldError.getId());
    }
}
