/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;

public class JobAuditModelTest {
    private final String testId = "test-id";
    private final String testConfigId = "test-config-id";
    private final String testName = "test-name";
    private final String testEventType = "test-event-name";
    private final AuditJobStatusModel testAuditJobStatusModel = Mockito.mock(AuditJobStatusModel.class);
    private final String testErrorMessage = "test-error-message";
    private final String testErrorStackTrace = "test-error-stack-trace";
    private JobAuditModel testJobAuditModel = new JobAuditModel();

    @BeforeEach
    public void init() {
        testJobAuditModel = new JobAuditModel(testId, testConfigId, testName, testEventType, testAuditJobStatusModel, testErrorMessage, testErrorStackTrace);
    }

    @Test
    public void getConfigIdTest() {
        assertEquals(testConfigId, testJobAuditModel.getConfigId());
    }

    @Test
    public void getNameTest() {
        assertEquals(testName, testJobAuditModel.getName());
    }

    @Test
    public void getEventTypeTest() {
        assertEquals(testEventType, testJobAuditModel.getEventType());
    }

    @Test
    public void getAuditJobStatusModelTest() {
        assertEquals(testAuditJobStatusModel, testJobAuditModel.getAuditJobStatusModel());
    }

    @Test
    public void getErrorMessageTest() {
        assertEquals(testErrorMessage, testJobAuditModel.getErrorMessage());
    }

    @Test
    public void getErrorStackTraceTest() {
        assertEquals(testErrorStackTrace, testJobAuditModel.getErrorStackTrace());
    }
}

