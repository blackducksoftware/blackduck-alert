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

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class AuditSuccessEventListenerTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final TaskExecutor taskExecutor = new SyncTaskExecutor();

    private ExecutingJobManager executingJobManager;
    private JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor;
    private AuditSuccessHandler handler;

    @BeforeEach
    public void init() {
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);
        jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
        handler = new AuditSuccessHandler(executingJobManager);
    }

    @Test
    void onMessageTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        ExecutingJob executingJob = executingJobManager.startJob(jobId, notificationIds.size());
        UUID executingJobId = executingJob.getExecutionId();

        AuditSuccessEventListener listener = new AuditSuccessEventListener(gson, taskExecutor, handler);
        AuditSuccessEvent event = new AuditSuccessEvent(executingJobId, jobId, notificationIds);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        JobCompletionStatusModel statusModel = jobCompletionStatusModelAccessor.getJobExecutionStatus(jobId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS.name(), statusModel.getLatestStatus());
        assertEquals(1, statusModel.getSuccessCount());
        assertEquals(0, statusModel.getFailureCount());
        assertEquals(3, statusModel.getTotalNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(executingJobId).isEmpty());
    }
}
