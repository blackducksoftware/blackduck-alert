/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.audit.AuditFailedEvent;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.util.AuditStackTraceUtil;

public abstract class JobSubTaskEventHandler<T extends JobSubTaskEvent> implements AlertEventHandler<T> {
    private final Logger logger = LoggerFactory.getLogger(JobSubTaskEventHandler.class);
    private final EventManager eventManager;
    private final JobStage jobStage;
    private final ExecutingJobManager executingJobManager;

    protected JobSubTaskEventHandler(EventManager eventManager, JobStage jobStage, ExecutingJobManager executingJobManager) {
        this.eventManager = eventManager;
        this.jobStage = jobStage;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public final void handle(T event) {
        UUID jobExecutionId = event.getJobExecutionId();
        executingJobManager.decrementRemainingEvents(jobExecutionId);
        try {
            handleEvent(event);
            if (!executingJobManager.hasRemainingEvents(jobExecutionId)) {
                executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
                executingJobManager.getExecutingJob(jobExecutionId)
                    .filter(Predicate.not(ExecutingJob::isCompleted))
                    .ifPresent(executingJob -> {
                        executingJobManager.updateJobStatus(jobExecutionId, AuditEntryStatus.SUCCESS);
                        executingJobManager.endJob(jobExecutionId, Instant.now());
                    });
            }
        } catch (AlertException exception) {
            executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
            executingJobManager.incrementSentNotificationCount(jobExecutionId, event.getNotificationIds().size());
            eventManager.sendEvent(new AuditFailedEvent(
                jobExecutionId,
                event.getJobId(),
                event.getNotificationIds(),
                exception.getMessage(),
                AuditStackTraceUtil.createStackTraceString(exception)
            ));
        } catch (Exception exception) {
            handleUnknownException(exception, jobExecutionId, event);
        }
    }

    protected abstract void handleEvent(T event) throws AlertException;

    protected void handleUnknownException(Exception e, UUID jobExecutionId, T event) {
        logger.error("An unexpected error occurred while handling the following issue tracker event: {}.", event.getEventId(), e);
        executingJobManager.endStage(jobExecutionId, jobStage, Instant.now());
        executingJobManager.incrementSentNotificationCount(jobExecutionId, event.getNotificationIds().size());
        eventManager.sendEvent(new AuditFailedEvent(
            event.getJobExecutionId(),
            event.getJobId(),
            event.getNotificationIds(),
            "An unexpected error occurred during issue tracker distribution. Please refer to the logs for more details.",
            AuditStackTraceUtil.createStackTraceString(e)
        ));
    }
}
