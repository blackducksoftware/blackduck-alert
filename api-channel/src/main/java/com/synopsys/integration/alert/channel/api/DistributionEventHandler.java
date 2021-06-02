/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public class DistributionEventHandler<D extends DistributionJobDetailsModel> implements AlertEventHandler<DistributionEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DistributionChannel<D> channel;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final ProcessingAuditAccessor auditAccessor;

    public DistributionEventHandler(DistributionChannel<D> channel, JobDetailsAccessor<D> jobDetailsAccessor, ProcessingAuditAccessor auditAccessor) {
        this.channel = channel;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.auditAccessor = auditAccessor;
    }

    @Override
    public final void handle(DistributionEvent event) {
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                channel.distributeMessages(details.get(), event.getProviderMessages());
                auditAccessor.setAuditEntrySuccess(event.getJobId(), event.getNotificationIds());
            } catch (AlertException alertException) {
                handleAlertException(alertException, event);
            } catch (Exception unknownException) {
                handleUnknownException(unknownException, event);
            }
        } else {
            handleJobDetailsMissing(event);
        }
    }

    protected void handleAlertException(AlertException e, DistributionEvent event) {
        logger.error("An exception occurred while handling the following event: {}", event, e);
        auditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), "An exception occurred during message distribution", e);
    }

    protected void handleUnknownException(Exception e, DistributionEvent event) {
        logger.error("An unexpected error occurred while handling the following event: {}", event, e);
        auditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), "An unexpected error occurred during message distribution. Please refer to the logs for more details.", null);
    }

    protected void handleJobDetailsMissing(DistributionEvent event) {
        String failureMessage = "Received a distribution event for a Job that no longer exists";
        logger.warn("{}. Destination: {}", failureMessage, event.getDestination());
        auditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), failureMessage, null);
    }

}
