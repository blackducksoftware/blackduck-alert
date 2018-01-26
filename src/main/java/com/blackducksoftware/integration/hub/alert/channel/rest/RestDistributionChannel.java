/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.channel.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

import okhttp3.Request;

public abstract class RestDistributionChannel<E extends AbstractChannelEvent, G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends DistributionChannel<E, G, C> {
    private static final Logger logger = LoggerFactory.getLogger(RestDistributionChannel.class);

    final ChannelRestConnectionFactory channelRestConnectionFactory;

    public RestDistributionChannel(final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final SimpleKeyRepositoryWrapper<G, ?> globalRepository, final SimpleKeyRepositoryWrapper<C, ?> distributionRepository,
            final CommonDistributionRepositoryWrapper commonDistributionRepository, final Class<E> clazz, final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(gson, auditEntryRepository, globalRepository, distributionRepository, commonDistributionRepository, clazz);
        this.channelRestConnectionFactory = channelRestConnectionFactory;
    }

    @Override
    public void sendMessage(final E event, final C config) {
        try {
            final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection(getApiUrl());
            final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);

            final Request request = createRequest(channelRequestHelper, config, event.getProjectData());
            channelRequestHelper.sendMessageRequest(request, event.getTopic());
            setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final IntegrationException e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);

            if (e instanceof IntegrationRestException) {
                logger.error(((IntegrationRestException) e).getHttpStatusCode() + ":" + ((IntegrationRestException) e).getHttpStatusMessage());
            }
            logger.error(e.getMessage(), e);
        } catch (final Exception e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            logger.error(e.getMessage(), e);
        }
    }

    public abstract String getApiUrl();

    public abstract Request createRequest(final ChannelRequestHelper channelRequestHelper, final C config, final ProjectData projectData);

}
