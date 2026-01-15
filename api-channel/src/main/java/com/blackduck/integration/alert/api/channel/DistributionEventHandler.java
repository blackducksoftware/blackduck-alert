/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.audit.AuditFailedEvent;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.api.distribution.execution.JobStageEndedEvent;
import com.blackduck.integration.alert.api.distribution.execution.JobStageStartedEvent;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.distribute.DistributionEvent;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.util.AuditStackTraceUtil;

public class DistributionEventHandler<D extends DistributionJobDetailsModel> implements AlertEventHandler<DistributionEvent> {
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final DistributionChannel<D> channel;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final EventManager eventManager;

    public DistributionEventHandler(DistributionChannel<D> channel, JobDetailsAccessor<D> jobDetailsAccessor, EventManager eventManager) {
        this.channel = channel;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.eventManager = eventManager;

    }

    @Override
    public final void handle(DistributionEvent event) {
        UUID jobExecutionId = event.getJobExecutionId();
        eventManager.sendEvent(new JobStageStartedEvent(jobExecutionId, JobStage.CHANNEL_PROCESSING, Instant.now().toEpochMilli()));
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                notificationLogger.debug("Channel: {} is processing event: {}", channel.getClass(), event.getEventId());
                notificationLogger.debug("Event: [{}] contains the following notificationIds: [{}]", event.getEventId(), event.getNotificationIds());
                channel.distributeMessages(
                    details.get(),
                    event.getProviderMessages(),
                    event.getJobName(),
                    event.getJobId(),
                    jobExecutionId,
                    event.getNotificationIds()
                );
                notificationLogger.debug("Channel: {} successfully processed event: {}", channel.getClass(), event.getEventId());
            } catch (AlertException alertException) {
                handleAlertException(alertException, event);
            } catch (Exception unknownException) {
                handleUnknownException(unknownException, event);
            }
        } else {
            handleJobDetailsMissing(event);
        }
        eventManager.sendEvent(new JobStageEndedEvent(jobExecutionId, JobStage.CHANNEL_PROCESSING, Instant.now().toEpochMilli()));
    }

    protected void handleAlertException(AlertException e, DistributionEvent event) {
        notificationLogger.error("An exception occurred while handling the following event: {}.", event.getEventId(), e);
        eventManager.sendEvent(new AuditFailedEvent(
            event.getJobExecutionId(),
            event.getJobId(),
            event.getNotificationIds(),
            String.format("An exception occurred while handling the following event: %s.", event.getEventId()),
            AuditStackTraceUtil.createStackTraceString(e)
        ));
    }

    protected void handleUnknownException(Exception e, DistributionEvent event) {
        notificationLogger.error("An unexpected error occurred while handling the following event: {}.", event.getEventId(), e);
        eventManager.sendEvent(new AuditFailedEvent(
            event.getJobExecutionId(),
            event.getJobId(),
            event.getNotificationIds(),
            "An unexpected error occurred during message distribution. Please refer to the logs for more details.",
            AuditStackTraceUtil.createStackTraceString(e)
        ));
    }

    protected void handleJobDetailsMissing(DistributionEvent event) {
        eventManager.sendEvent(new AuditFailedEvent(
            event.getJobExecutionId(),
            event.getJobId(),
            event.getNotificationIds(),
            "Received a distribution event for a Job that no longer exists",
            null
        ));
    }

}
