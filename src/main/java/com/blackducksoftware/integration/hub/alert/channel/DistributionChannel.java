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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.google.gson.Gson;

public abstract class DistributionChannel<E extends AbstractChannelEvent, G extends DatabaseEntity, C extends DatabaseEntity> extends MessageReceiver<E> {
    private final static Logger logger = LoggerFactory.getLogger(DistributionChannel.class);

    private final JpaRepository<G, Long> globalRepository;
    private final CommonDistributionRepository commonDistributionRepository;
    private G globalConfigEntity;

    public DistributionChannel(final Gson gson, final JpaRepository<G, Long> globalRepository, final CommonDistributionRepository commonDistributionRepository, final Class<E> clazz) {
        super(gson, clazz);
        this.globalRepository = globalRepository;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public CommonDistributionRepository getCommonDistributionRepository() {
        return commonDistributionRepository;
    }

    public G getGlobalConfigEntity() {
        if (globalConfigEntity == null) {
            final List<G> globalConfigs = globalRepository.findAll();
            if (globalConfigs.size() == 1) {
                globalConfigEntity = globalConfigs.get(0);
            }
            logger.error("Global Config did not have the expected number of rows: Expected 1, but found {}.", globalConfigs.size());
        }

        return globalConfigEntity;
    }

    public abstract void sendMessage(final E event, final C config);

    public abstract String testMessage(final G globalConfig) throws IntegrationException;

    public abstract void handleEvent(final E event);

    @Override
    public void receiveMessage(final String message) {
        logger.info(String.format("Received %s event message: %s", getClass().getName(), message));
        final E event = getEvent(message);
        logger.info(String.format("%s event %s", getClass().getName(), event));

        handleEvent(event);
    }

}
