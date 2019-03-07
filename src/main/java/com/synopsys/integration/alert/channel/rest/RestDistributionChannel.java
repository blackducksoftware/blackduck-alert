/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

// TODO this class should not be part of the hierarchy. It should be used as a helper class to help use rest and all channels should extends DistributionChannel
public abstract class RestDistributionChannel extends DistributionChannel {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChannelRestConnectionFactory channelRestConnectionFactory;

    public RestDistributionChannel(final String distributionType, final Gson gson, final AlertProperties alertProperties, final DefaultAuditUtility auditUtility,
        final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(distributionType, gson, alertProperties, auditUtility);
        this.channelRestConnectionFactory = channelRestConnectionFactory;
    }

    @Override
    public void sendMessage(final DistributionEvent event) throws IntegrationException {
        try {
            final IntHttpClient intHttpClient = channelRestConnectionFactory.createIntHttpClient();
            final List<Request> requests = createRequests(event);
            for (final Request request : requests) {
                sendMessageRequest(intHttpClient, request, event.getDestination());
            }
        } catch (final Exception ex) {
            throw new AlertException(ex);
        }
    }

    public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String jsonString) {
        return createPostMessageRequest(url, headers, null, jsonString);
    }

    public Request createPostMessageRequest(final String url, final Map<String, String> headers, final Map<String, Set<String>> queryParameters) {
        return createPostMessageRequest(url, headers, queryParameters, null);
    }

    public Request createPostMessageRequest(final String url, final Map<String, String> headers, final Map<String, Set<String>> queryParameters, final String jsonString) {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.POST).uri(url).additionalHeaders(headers);
        if (queryParameters != null && !queryParameters.isEmpty()) {
            requestBuilder.queryParameters(queryParameters);
        }
        if (jsonString != null) {
            requestBuilder.bodyContent(new StringBodyContent(jsonString));
        }
        return requestBuilder.build();
    }

    public void sendMessageRequest(final IntHttpClient intHttpClient, final Request request, final String messageType) throws IntegrationException {
        logger.info("Attempting to send a {} message...", messageType);
        try (final Response response = sendGenericRequest(intHttpClient, request)) {
            if (RestConstants.OK_200 <= response.getStatusCode() && response.getStatusCode() < RestConstants.BAD_REQUEST_400) {
                logger.info("Successfully sent a {} message!", messageType);
            }
        } catch (final IOException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public Response sendGenericRequest(final IntHttpClient intHttpClient, final Request request) throws IntegrationException {
        try (final Response response = intHttpClient.execute(request)) {
            logger.trace("Response: {}", response);
            return response;
        } catch (final Exception e) {
            logger.error("Error sending request", e);
            throw new AlertException(e.getMessage(), e);
        }
    }

    public ChannelRestConnectionFactory getChannelRestConnectionFactory() {
        return channelRestConnectionFactory;
    }

    public abstract List<Request> createRequests(final DistributionEvent event) throws IntegrationException;

}
