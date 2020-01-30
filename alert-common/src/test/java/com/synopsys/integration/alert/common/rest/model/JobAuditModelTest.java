package com.synopsys.integration.alert.common.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;

public class JobAuditModelTest {
    String testId = "test-id";
    String testConfigId = "test-config-id";
    String testName = "test-name";
    String testEventType = "test-event-name";
    AuditJobStatusModel testAuditJobStatusModel = Mockito.mock(AuditJobStatusModel.class);
    String testErrorMessage = "test-error-message";
    String testErrorStackTrace = "test-error-stack-trace";
    JobAuditModel testJobAuditModel = new JobAuditModel();

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

