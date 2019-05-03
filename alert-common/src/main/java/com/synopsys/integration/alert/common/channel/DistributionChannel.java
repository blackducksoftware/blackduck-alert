/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class DistributionChannel extends MessageReceiver {
    private static final Logger logger = LoggerFactory.getLogger(DistributionChannel.class);
    private final AuditUtility auditUtility;
    private final AlertProperties alertProperties;
    private final String destinationName;

    public DistributionChannel(final String destinationName, final Gson gson, final AlertProperties alertProperties, final AuditUtility auditUtility) {
        super(gson);
        this.destinationName = destinationName;
        this.alertProperties = alertProperties;
        this.auditUtility = auditUtility;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public AlertProperties getAlertProperties() {
        return alertProperties;
    }

    @Override
    public void handleEvent(final DistributionEvent event) {
        if (event.getDestination().equals(getDestinationName())) {
            try {
                sendAuditedMessage(event);
            } catch (final IntegrationException ex) {
                logger.error("There was an error sending the message.", ex);
            }
        } else {
            logger.warn("Received an event for channel '{}', but this channel is '{}'.", event.getDestination(), getDestinationName());
        }

    }

    public void sendAuditedMessage(final DistributionEvent event) throws IntegrationException {
        try {
            sendMessage(event);
            auditUtility.setAuditEntrySuccess(event.getAuditIds());
        } catch (final IntegrationRestException irex) {
            auditUtility.setAuditEntryFailure(event.getAuditIds(), irex.getMessage(), irex);
            logger.error("{} : {}", irex.getHttpStatusCode(), irex.getHttpStatusMessage());
            throw new AlertException(irex.getMessage());
        } catch (final AlertException e) {
            auditUtility.setAuditEntryFailure(event.getAuditIds(), e.getMessage(), e);
            // TODO this will always be caught by the next catch block
            throw e;
        } catch (final Exception e) {
            auditUtility.setAuditEntryFailure(event.getAuditIds(), e.getMessage(), e);
            throw new AlertException(e.getMessage());
        }
    }

    public abstract void sendMessage(final DistributionEvent event) throws IntegrationException;

}
