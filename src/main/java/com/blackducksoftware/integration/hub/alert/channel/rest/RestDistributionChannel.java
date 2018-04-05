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
package com.blackducksoftware.integration.hub.alert.channel.rest;

import java.util.Collection;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;

public abstract class RestDistributionChannel<E extends AbstractChannelEvent, G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends DistributionChannel<E, G, C> {
    final ChannelRestConnectionFactory channelRestConnectionFactory;

    public RestDistributionChannel(final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final SimpleKeyRepositoryWrapper<G, ?> globalRepository, final SimpleKeyRepositoryWrapper<C, ?> distributionRepository,
            final CommonDistributionRepositoryWrapper commonDistributionRepository, final Class<E> clazz, final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(gson, auditEntryRepository, globalRepository, distributionRepository, commonDistributionRepository, clazz);
        this.channelRestConnectionFactory = channelRestConnectionFactory;
    }

    @Override
    public void sendMessage(final E event, final C config) throws Exception {
        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection(getApiUrl());
        final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);

        final Request request = createRequest(channelRequestHelper, config, event.getProjectData());
        channelRequestHelper.sendMessageRequest(request, event.getTopic());
    }

    public abstract String getApiUrl();

    public abstract Request createRequest(final ChannelRequestHelper channelRequestHelper, final C config, final Collection<ProjectData> projectData) throws IntegrationException;

}
