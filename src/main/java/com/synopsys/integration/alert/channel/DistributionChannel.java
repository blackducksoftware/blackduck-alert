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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.alert.workflow.MessageReceiver;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public abstract class DistributionChannel<G extends GlobalChannelConfigEntity> extends MessageReceiver {
    private static final Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final JpaRepository<G, Long> globalRepository;
    private final AuditUtility auditUtility;
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;

    public DistributionChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility, final JpaRepository<G, Long> globalRepository) {
        super(gson);
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.auditUtility = auditUtility;
        this.globalRepository = globalRepository;
    }

    public abstract String getDistributionType();

    public AlertProperties getAlertProperties() {
        return alertProperties;
    }

    public BlackDuckProperties getBlackDuckProperties() {
        return blackDuckProperties;
    }

    @Transactional
    public G getGlobalConfigEntity() {
        if (globalRepository != null) {
            final List<G> globalConfigs = globalRepository.findAll();
            if (globalConfigs.size() == 1) {
                return globalConfigs.get(0);
            }
            logger.error("Global Config did not have the expected number of rows: Expected 1, but found {}.", globalConfigs.size());
        }
        return null;
    }

    @Override
    public void handleEvent(final DistributionEvent event) {
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

    public void sendAuditedMessage(final DistributionEvent event) throws IntegrationException {
        try {
            sendMessage(event);
            auditUtility.setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final IntegrationRestException irex) {
            auditUtility.setAuditEntryFailure(event.getAuditEntryId(), irex.getMessage(), irex);
            logger.error("{} : {}", irex.getHttpStatusCode(), irex.getHttpStatusMessage());
            throw new AlertException(irex.getMessage());
        } catch (final Exception e) {
            auditUtility.setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            throw new AlertException(e.getMessage());
        }
    }

    public abstract void sendMessage(final DistributionEvent event) throws IntegrationException;

    public String testGlobalConfig(final TestConfigModel testConfig) throws IntegrationException {
        if (testConfig.getRestModel() != null) {
            throw new AlertException("Test method not implemented.");
        }
        return "The provided config was null.";
    }
}
