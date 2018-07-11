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
package com.blackducksoftware.integration.alert.channel.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.rest.HttpMethod;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.BodyContent;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;

public class ChannelRequestHelper {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRequestHelper.class);
    private final HubServicesFactory hubServicesFactory;

    public ChannelRequestHelper(final RestConnection restConnection) {
        hubServicesFactory = new HubServicesFactory(restConnection);
    }

    public Request createPostMessageRequest(final String url, final Map<String, String> headers, final String jsonString) {
        Request.Builder requestBuilder = new Request.Builder();
        final BodyContent bodyContent = new BodyContent(jsonString);
        requestBuilder = requestBuilder.method(HttpMethod.POST).uri(url).additionalHeaders(headers).bodyContent(bodyContent);
        final Request request = requestBuilder.build();
        return request;
    }

    public Request createPostMessageRequest(final String url, final Map<String, String> headers, final Map<String, String> queryParameters, final String jsonString) {
        Request.Builder requestBuilder = new Request.Builder();
        final BodyContent bodyContent = new BodyContent(jsonString);
        requestBuilder = requestBuilder.method(HttpMethod.POST).uri(url).additionalHeaders(headers).queryParameters(queryParameters).bodyContent(bodyContent);
        final Request request = requestBuilder.build();
        return request;
    }

    public void sendMessageRequest(final Request request, final String messageType) throws IntegrationException {
        logger.info("Attempting to send a {} message...", messageType);
        final Response response = sendGenericRequest(request);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
            logger.info("Successfully sent a {} message!", messageType);
        }
    }

    public Response sendGenericRequest(final Request request) throws IntegrationException {
        Response response = null;
        try {
            final HubService service = hubServicesFactory.createHubService();
            response = service.executeRequest(request);
            logger.trace("Response: " + response.toString());
            return response;
        } catch (final Exception generalException) {
            logger.error("Error sending request", generalException);
            throw new AlertException(generalException.getMessage());
        }
    }
}
