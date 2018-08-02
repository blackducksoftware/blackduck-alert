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
package com.blackducksoftware.integration.alert.channel;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.workflow.MessageReceiver;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

@Transactional
public abstract class DistributionChannel<G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends MessageReceiver<ChannelEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final JpaRepository<G, Long> globalRepository;
    private final JpaRepository<C, Long> distributionRepository;
    private final CommonDistributionRepository commonDistributionRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final ContentConverter contentExtractor;
    private final BlackDuckProperties blackDuckProperties;

    public DistributionChannel(final Gson gson, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final JpaRepository<G, Long> globalRepository,
            final JpaRepository<C, Long> distributionRepository,
            final CommonDistributionRepository commonDistributionRepository, final ContentConverter contentExtractor) {
        super(gson, ChannelEvent.class);
        this.blackDuckProperties = blackDuckProperties;
        this.auditEntryRepository = auditEntryRepository;
        this.globalRepository = globalRepository;
        this.distributionRepository = distributionRepository;
        this.commonDistributionRepository = commonDistributionRepository;
        this.contentExtractor = contentExtractor;
    }

    public AuditEntryRepository getAuditEntryRepository() {
        return auditEntryRepository;
    }

    public CommonDistributionRepository getCommonDistributionRepository() {
        return commonDistributionRepository;
    }

    public BlackDuckProperties getGlobalProperties() {
        return blackDuckProperties;
    }

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
    public void handleEvent(final ChannelEvent event) {
        final Long eventDistributionId = event.getCommonDistributionConfigId();
        final Optional<CommonDistributionConfigEntity> commonDistributionEntity = getCommonDistributionRepository().findById(eventDistributionId);
        if (commonDistributionEntity.isPresent()) {
            if (event.getDestination().equals(commonDistributionEntity.get().getDistributionType())) {
                try {
                    final Long channelDistributionConfigId = commonDistributionEntity.get().getDistributionConfigId();
                    final C channelDistributionEntity = distributionRepository.getOne(channelDistributionConfigId);
                    sendAuditedMessage(event, channelDistributionEntity);
                } catch (final IntegrationException ex) {
                    logger.error("There was an error sending the message.", ex);
                }
            } else {
                logger.warn("Received an event of type '{}', but the retrieved configuration was for an event of type '{}'.", event.getDestination(), commonDistributionEntity.get().getDistributionType());
            }
        } else {
            logger.error("Event distribution ID not found {}", eventDistributionId);
        }
    }

    public void sendAuditedMessage(final ChannelEvent event, final C config) throws IntegrationException {
        try {
            sendMessage(event, config);
            setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final IntegrationRestException irex) {
            setAuditEntryFailure(event.getAuditEntryId(), irex.getMessage(), irex);
            logger.error("{} : {}", irex.getHttpStatusCode(), irex.getHttpStatusMessage());
            logger.error(irex.getMessage(), irex);
            throw new AlertException(irex.getMessage());
        } catch (final Exception e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            logger.error(e.getMessage(), e);
            throw new AlertException(e.getMessage());
        }
    }

    public abstract void sendMessage(final ChannelEvent event, final C config) throws IntegrationException;

    public String testGlobalConfig(final G entity) throws IntegrationException {
        if (entity != null) {
            throw new AlertException("Test method not implemented.");
        }
        return "The provided entity was null.";
    }

    public void setAuditEntrySuccess(final Long auditEntryId) {
        if (auditEntryId != null) {
            try {
                final Optional<AuditEntryEntity> auditEntryEntityOptional = getAuditEntryRepository().findById(auditEntryId);
                if (auditEntryEntityOptional.isPresent()) {
                    final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.get();
                    auditEntryEntity.setStatus(AuditEntryStatus.SUCCESS);
                    auditEntryEntity.setErrorMessage(null);
                    auditEntryEntity.setErrorStackTrace(null);
                    auditEntryEntity.setTimeLastSent(new Date(System.currentTimeMillis()));
                    getAuditEntryRepository().save(auditEntryEntity);
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void setAuditEntryFailure(final Long auditEntryId, final String errorMessage, final Throwable t) {
        if (auditEntryId != null) {
            try {
                final Optional<AuditEntryEntity> auditEntryEntityOptional = getAuditEntryRepository().findById(auditEntryId);
                if (auditEntryEntityOptional.isPresent()) {
                    final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.get();
                    auditEntryEntity.setStatus(AuditEntryStatus.FAILURE);
                    auditEntryEntity.setErrorMessage(errorMessage);
                    final String[] rootCause = ExceptionUtils.getRootCauseStackTrace(t);
                    String exceptionStackTrace = "";
                    for (final String line : rootCause) {
                        if (exceptionStackTrace.length() + line.length() < AuditEntryEntity.STACK_TRACE_CHAR_LIMIT) {
                            exceptionStackTrace = String.format("%s%s%s", exceptionStackTrace, line, System.lineSeparator());
                        } else {
                            break;
                        }
                    }

                    auditEntryEntity.setErrorStackTrace(exceptionStackTrace);

                    auditEntryEntity.setTimeLastSent(new Date(System.currentTimeMillis()));
                    getAuditEntryRepository().save(auditEntryEntity);
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public <C> Optional<C> extractContentFromEvent(final ChannelEvent event, final Class<C> contentClass) {
        return Optional.ofNullable(contentExtractor.getJsonContent(event.getContent(), contentClass));
    }

}
