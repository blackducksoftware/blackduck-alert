/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;

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
