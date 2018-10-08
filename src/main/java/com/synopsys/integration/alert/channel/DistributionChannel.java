/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.workflow.MessageReceiver;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class DistributionChannel<E extends ChannelEvent> extends MessageReceiver<E> {
    private static final Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final AuditUtility auditUtility;
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;

    public DistributionChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility,
        final Class eventClass) {
        super(gson, eventClass);
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.auditUtility = auditUtility;
    }

    public abstract String getDistributionType();

    public AlertProperties getAlertProperties() {
        return alertProperties;
    }

    public BlackDuckProperties getBlackDuckProperties() {
        return blackDuckProperties;
    }

    @Override
    public void handleEvent(final E event) {
        if (event.getDestination().equals(getDistributionType())) {
            try {
                sendAuditedMessage(event);
            } catch (final IntegrationException ex) {
                logger.error("There was an error sending the message.", ex);
            }
        } else {
            logger.warn("Received an event of type '{}', but this channel is for type '{}'.", event.getDestination(), getDistributionType());
        }

    }

    public void sendAuditedMessage(final E event) throws IntegrationException {
        try {
            sendMessage(event);
            auditUtility.setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final IntegrationRestException irex) {
            auditUtility.setAuditEntryFailure(event.getAuditEntryId(), irex.getMessage(), irex);
            logger.error("{} : {}", irex.getHttpStatusCode(), irex.getHttpStatusMessage());
            logger.error(irex.getMessage(), irex);
            throw new AlertException(irex.getMessage());
        } catch (final Exception e) {
            auditUtility.setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            logger.error(e.getMessage(), e);
            throw new AlertException(e.getMessage());
        }
    }

    public abstract void sendMessage(final E event) throws IntegrationException;

    public String testGlobalConfig(final Config restModel) throws IntegrationException {
        if (restModel != null) {
            throw new AlertException("Test method not implemented.");
        }
        return "The provided config was null.";
    }
}
