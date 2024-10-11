/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;

class AuditSuccessHandlerTest {
    private ExecutingJobManager executingJobManager;
    private JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor;

    @BeforeEach
    public void init() {
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);
        jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
    }

    @Test
    void handleEventTest() {
        UUID jobId = UUID.randomUUID();
        ExecutingJob executingJob = executingJobManager.startJob(jobId, 0);
        UUID jobExecutionId = executingJob.getExecutionId();
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, jobId, Set.of());
        handler.handle(event);
        JobCompletionStatusModel statusModel = jobCompletionStatusModelAccessor.getJobExecutionStatus(jobId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS.name(), statusModel.getLatestStatus());
        assertEquals(1, statusModel.getSuccessCount());
        assertEquals(0, statusModel.getFailureCount());
        assertEquals(0, statusModel.getTotalNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    @Test
    void handleEventAuditMissingTest() {
        UUID jobId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        AlertPagedQueryDetails pagedQueryDetails = new AlertPagedQueryDetails(1, 10);
        AuditSuccessHandler handler = new AuditSuccessHandler(executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, jobId, notificationIds);
        handler.handle(event);
        Optional<ExecutingJob> executingJob = executingJobManager.getExecutingJob(jobExecutionId);
        assertTrue(executingJob.isEmpty());
        assertTrue(jobCompletionStatusModelAccessor.getJobExecutionStatus(pagedQueryDetails).getModels().isEmpty());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }
}
