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
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.AlertChannelEventListener;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEventV2;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends MessageReceiver<DistributionEventV2> implements AlertChannelEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuditAccessor auditAccessor;
    private final JobDetailsAccessor<D> jobDetailsAccessor;
    private final DistributionChannelV2<D> channel;
    private final ChannelKey channelKey;

    protected DistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, JobDetailsAccessor<D> jobDetailsAccessor, DistributionChannelV2<D> channel, ChannelKey channelKey) {
        super(gson, DistributionEventV2.class);
        this.auditAccessor = auditAccessor;
        this.jobDetailsAccessor = jobDetailsAccessor;
        this.channel = channel;
        this.channelKey = channelKey;
    }

    @Override
    public final void handleEvent(DistributionEventV2 event) {
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
        //FIXME remove this when the old channels are deprecated
        return channelKey.getUniversalKey() + "_v2";
    }

    protected void handleException(AlertException e, DistributionEventV2 event) {
        logger.error("An exception occurred while handling the following event: {}", event, e);
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), "An exception occurred during message distribution", e);
    }

    protected void handleJobDetailsMissing(DistributionEventV2 event) {
        String failureMessage = "Received a distribution event for a Job that no longer exists";
        logger.warn("{}. Destination: {}", failureMessage, event.getDestination());
        auditAccessor.setAuditEntryFailure(Set.of(event.getAuditId()), failureMessage, null);
    }

}
