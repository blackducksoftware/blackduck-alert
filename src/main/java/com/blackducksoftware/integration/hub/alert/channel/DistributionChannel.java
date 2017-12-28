/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.datasource.entity.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.google.gson.Gson;

public abstract class DistributionChannel<E extends AbstractChannelEvent, G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends MessageReceiver<E> {
    private final static Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final JpaRepository<G, Long> globalRepository;
    private final JpaRepository<C, Long> distributionRepository;
    private final CommonDistributionRepository commonDistributionRepository;
    private final AuditEntryRepository auditEntryRepository;

    public DistributionChannel(final Gson gson, final AuditEntryRepository auditEntryRepository, final JpaRepository<G, Long> globalRepository, final JpaRepository<C, Long> distributionRepository,
            final CommonDistributionRepository commonDistributionRepository, final Class<E> clazz) {
        super(gson, clazz);
        this.auditEntryRepository = auditEntryRepository;
        this.globalRepository = globalRepository;
        this.distributionRepository = distributionRepository;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public AuditEntryRepository getAuditEntryRepository() {
        return auditEntryRepository;
    }

    public CommonDistributionRepository getCommonDistributionRepository() {
        return commonDistributionRepository;
    }

    public void setAuditEntrySuccess(final Long auditEntryId) {
        if (auditEntryId != null) {
            final AuditEntryEntity auditEntryEntity = getAuditEntryRepository().findOne(auditEntryId);
            if (auditEntryEntity != null) {
                logger.error("AUDIT ENTRY WAS NOT NULL setting success");
                auditEntryEntity.setStatus(StatusEnum.SUCCESS);

                auditEntryEntity.setTimeLastSent(new Date());
                getAuditEntryRepository().save(auditEntryEntity);
            }
        }
    }

    public void setAuditEntryFailure(final Long auditEntryId, final String errorMessage, final Throwable e) {
        if (auditEntryId != null) {
            final AuditEntryEntity auditEntryEntity = getAuditEntryRepository().findOne(auditEntryId);
            if (auditEntryEntity != null) {
                logger.error("AUDIT ENTRY WAS NOT NULL setting failure");
                auditEntryEntity.setStatus(StatusEnum.FAILURE);
                auditEntryEntity.setErrorMessage(errorMessage);
                final StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                auditEntryEntity.setErrorStackTrace(stringWriter.toString());

                auditEntryEntity.setTimeLastSent(new Date());
                getAuditEntryRepository().save(auditEntryEntity);
            }
        }
    }

    public G getGlobalConfigEntity() {
        final List<G> globalConfigs = globalRepository.findAll();
        if (globalConfigs.size() == 1) {
            return globalConfigs.get(0);
        }
        logger.error("Global Config did not have the expected number of rows: Expected 1, but found {}.", globalConfigs.size());
        return null;
    }

    @Override
    public void receiveMessage(final String message) {
        logger.info(String.format("Received %s event message: %s", getClass().getName(), message));
        final E event = getEvent(message);
        logger.info(String.format("%s event %s", getClass().getName(), event));

        handleEvent(event);
    }

    public void handleEvent(final E event) {
        final Long eventDistributionId = event.getCommonDistributionConfigId();
        final CommonDistributionConfigEntity commonDistributionEntity = getCommonDistributionRepository().findOne(eventDistributionId);
        if (event.getTopic().equals(commonDistributionEntity.getDistributionType())) {
            final Long channelDistributionConfigId = commonDistributionEntity.getDistributionConfigId();
            final C channelDistributionEntity = distributionRepository.findOne(channelDistributionConfigId);
            sendMessage(event, channelDistributionEntity);
        } else {
            logger.warn("Received an event of type '{}', but the retrieved configuration was for an event of type '{}'.", event.getTopic(), commonDistributionEntity.getDistributionType());
        }
    }

    public abstract void sendMessage(final E event, final C config);

}
