/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.AlertChannelEventListener;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class DistributionChannel extends MessageReceiver<DistributionEvent> implements AlertChannelEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AuditAccessor auditAccessor;

    public DistributionChannel(Gson gson, AuditAccessor auditAccessor) {
        super(gson, DistributionEvent.class);
        this.auditAccessor = auditAccessor;
    }

    @Override
    public void handleEvent(DistributionEvent event) {
        if (event.getDestination().equals(getDestinationName())) {
            logger.debug("Received an event for channel '{}''.", event.getDestination());
            try {
                sendAuditedMessage(event);
            } catch (IntegrationException ex) {
                logger.error("There was an error sending the message.", ex);
            }
        } else {
            logger.warn("Received an event for channel '{}', but this channel is '{}'.", event.getDestination(), getDestinationName());
        }
    }

    public void sendAuditedMessage(DistributionEvent event) throws IntegrationException {
        try {
            sendMessage(event);
            auditAccessor.setAuditEntrySuccess(event.getAuditIds());
        } catch (IntegrationRestException irex) {
            auditAccessor.setAuditEntryFailure(event.getAuditIds(), irex.getMessage(), irex);
            logger.error("{} : {}", irex.getHttpStatusCode(), irex.getHttpStatusMessage());
            throw new AlertException(irex.getMessage(), irex);
        } catch (Exception e) {
            logger.error("Error occurred sending message: ", e);
            auditAccessor.setAuditEntryFailure(event.getAuditIds(), e.getMessage(), e);
            throw new AlertException(e.getMessage(), e);
        }
    }

    public abstract MessageResult sendMessage(DistributionEvent event) throws IntegrationException;

}
