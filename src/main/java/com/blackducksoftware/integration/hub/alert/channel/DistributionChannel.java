/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

@Transactional
public abstract class DistributionChannel<E extends AbstractChannelEvent, G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends MessageReceiver<E> {
    private final static Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final SimpleKeyRepositoryWrapper<G, ?> globalRepository;
    private final SimpleKeyRepositoryWrapper<C, ?> distributionRepository;
    private final CommonDistributionRepositoryWrapper commonDistributionRepository;
    private final AuditEntryRepositoryWrapper auditEntryRepository;

    public DistributionChannel(final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final SimpleKeyRepositoryWrapper<G, ?> globalRepository, final SimpleKeyRepositoryWrapper<C, ?> distributionRepository,
            final CommonDistributionRepositoryWrapper commonDistributionRepository, final Class<E> clazz) {
        super(gson, clazz);
        this.auditEntryRepository = auditEntryRepository;
        this.globalRepository = globalRepository;
        this.distributionRepository = distributionRepository;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public AuditEntryRepositoryWrapper getAuditEntryRepository() {
        return auditEntryRepository;
    }

    public CommonDistributionRepositoryWrapper getCommonDistributionRepository() {
        return commonDistributionRepository;
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
    public void handleEvent(final E event) {
        final Long eventDistributionId = event.getCommonDistributionConfigId();
        final CommonDistributionConfigEntity commonDistributionEntity = getCommonDistributionRepository().findById(eventDistributionId);
        if (event.getTopic().equals(commonDistributionEntity.getDistributionType())) {
            try {
                final Long channelDistributionConfigId = commonDistributionEntity.getDistributionConfigId();
                final C channelDistributionEntity = distributionRepository.findById(channelDistributionConfigId);
                sendAuditedMessage(event, channelDistributionEntity);
            } catch (final IntegrationException ex) {
                logger.error("There was an error sending the message.", ex);
            }
        } else {
            logger.warn("Received an event of type '{}', but the retrieved configuration was for an event of type '{}'.", event.getTopic(), commonDistributionEntity.getDistributionType());
        }
    }

    public void sendAuditedMessage(final E event, final C config) throws IntegrationException {
        try {
            sendMessage(event, config);
            setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final Exception e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            if (e instanceof IntegrationRestException) {
                logger.error(((IntegrationRestException) e).getHttpStatusCode() + ":" + ((IntegrationRestException) e).getHttpStatusMessage());
            }
            logger.error(e.getMessage(), e);
            throw new AlertException(e.getMessage());
        }
    }

    public abstract void sendMessage(final E event, final C config) throws Exception;

    public String testGlobalConfig(final G entity) throws IntegrationException {
        if (entity != null) {
            return "Not implemented.";
        }
        return "The provided entity was null.";
    }

    public void setAuditEntrySuccess(final Long auditEntryId) {
        if (auditEntryId != null) {
            try {
                final AuditEntryEntity auditEntryEntity = getAuditEntryRepository().findById(auditEntryId);
                if (auditEntryEntity != null) {
                    auditEntryEntity.setStatus(StatusEnum.SUCCESS);
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
                final AuditEntryEntity auditEntryEntity = getAuditEntryRepository().findById(auditEntryId);
                if (auditEntryEntity != null) {
                    auditEntryEntity.setStatus(StatusEnum.FAILURE);
                    auditEntryEntity.setErrorMessage(errorMessage);
                    final String[] rootCause = ExceptionUtils.getRootCauseStackTrace(t);
                    String exceptionStackTrace = "";
                    for (final String line : rootCause) {
                        if (exceptionStackTrace.length() + line.length() < AuditEntryEntity.STACK_TRACE_CHAR_LIMIT) {
                            exceptionStackTrace = exceptionStackTrace + line + System.lineSeparator();
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
}
