/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.AlertChannelEventListener;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends MessageReceiver<DistributionEvent> implements AlertChannelEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProcessingAuditAccessor auditAccessor;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final DistributionChannel<D> channel;
    private final ChannelKey channelKey;

    protected DistributionEventReceiver(Gson gson, ProcessingAuditAccessor auditAccessor, JobDetailsAccessor<D> jobDetailsAccessor, DistributionChannel<D> channel, ChannelKey channelKey) {
        super(gson, DistributionEvent.class);
        this.auditAccessor = auditAccessor;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.channel = channel;
        this.channelKey = channelKey;
    }

    @Override
    public final void handleEvent(DistributionEvent event) {
        Optional<D> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                channel.distributeMessages(details.get(), event.getProviderMessages());
                auditAccessor.setAuditEntrySuccess(Set.of(event.getAuditId()));
            } catch (AlertException e) {
                handleException(e, event);
            }
        } else {
            handleJobDetailsMissing(event);
        }
    }

    @Override
    public final String getDestinationName() {
        return channelKey.getUniversalKey();
    }

    protected void handleException(AlertException e, DistributionEvent event) {
        logger.error("An exception occurred while handling the following event: {}", event, e);
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), "An exception occurred during message distribution", e);
    }

    protected void handleJobDetailsMissing(DistributionEvent event) {
        String failureMessage = "Received a distribution event for a Job that no longer exists";
        logger.warn("{}. Destination: {}", failureMessage, event.getDestination());
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), failureMessage, null);
    }

}
