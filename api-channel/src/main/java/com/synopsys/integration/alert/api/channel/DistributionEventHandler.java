/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;
import com.synopsys.integration.alert.telemetry.database.DefaultTelemetryAccessor;

public class DistributionEventHandler<D extends DistributionJobDetailsModel> implements AlertEventHandler<DistributionEvent> {
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final DistributionChannel<D> channel;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final ProcessingAuditAccessor auditAccessor;
    private final DefaultTelemetryAccessor telemetryAccessor;

    public DistributionEventHandler(
        DistributionChannel<D> channel,
        JobDetailsAccessor<D> jobDetailsAccessor,
        ProcessingAuditAccessor auditAccessor,
        DefaultTelemetryAccessor telemetryAccessor
    ) {
        this.channel = channel;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.auditAccessor = auditAccessor;
        this.telemetryAccessor = telemetryAccessor;
    }

    @Override
    public final void handle(DistributionEvent event) {
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                notificationLogger.trace("Channel: {} is processing event: {}", channel.getClass(), event.getEventId());
                telemetryAccessor.createDistributionTelemetryTask(UUID.fromString(event.getEventId()), event.getJobId(), event.getDestination());
                channel.distributeMessages(details.get(), event.getProviderMessages(), event.getJobName());
                auditAccessor.setAuditEntrySuccess(event.getJobId(), event.getNotificationIds());
                notificationLogger.trace("Channel: {} successfully processed event: {}", channel.getClass(), event.getEventId());
            } catch (AlertException alertException) {
                handleAlertException(alertException, event);
            } catch (Exception unknownException) {
                handleUnknownException(unknownException, event);
            } finally {
                telemetryAccessor.completeDistributionTelemetryTask(UUID.fromString(event.getEventId()));
            }
        } else {
            handleJobDetailsMissing(event);
        }
    }

    protected void handleAlertException(AlertException e, DistributionEvent event) {
        notificationLogger.error("An exception occurred while handling the following event: {}.", event.getEventId(), e);
        auditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), "An exception occurred during message distribution", e);
    }

    protected void handleUnknownException(Exception e, DistributionEvent event) {
        notificationLogger.error("An unexpected error occurred while handling the following event: {}.", event.getEventId(), e);
        auditAccessor.setAuditEntryFailure(
            event.getJobId(),
            event.getNotificationIds(),
            "An unexpected error occurred during message distribution. Please refer to the logs for more details.",
            null
        );
    }

    protected void handleJobDetailsMissing(DistributionEvent event) {
        String failureMessage = "Received a distribution event for a Job that no longer exists";
        notificationLogger.warn("{}. Destination: {}. Event: {}. Job: {}", failureMessage, event.getDestination(), event.getEventId(), event.getJobId());
        auditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), failureMessage, null);
    }

}
