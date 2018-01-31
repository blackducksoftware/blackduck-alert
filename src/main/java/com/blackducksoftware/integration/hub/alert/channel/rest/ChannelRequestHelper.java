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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChannelRequestHelper {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRequestHelper.class);
    private final RestConnection restConnection;

    public ChannelRequestHelper(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public HttpUrl createHttpUrl(final List<String> urlSegments) throws IntegrationException {
        try {
            return restConnection.createHttpUrl(urlSegments);
        } catch (final NullPointerException ex) {
            throw new IntegrationException("URL invalid: " + StringUtils.join(urlSegments, '/'));
        }
    }

    public HttpUrl createHttpUrl(final String url) throws IntegrationException {
        try {
            return restConnection.createHttpUrl(url);
        } catch (final NullPointerException ex) {
            throw new IntegrationException("URL invalid: " + url);
        }
    }

    public Request createMessageRequest(final List<String> urlSegments, final Map<String, String> headers, final String jsonString) throws IntegrationException {
        final HttpUrl httpUrl = createHttpUrl(urlSegments);
        return createMessageRequest(httpUrl, headers, jsonString);
    }

    public Request createMessageRequest(final String url, final Map<String, String> headers, final String jsonString) throws IntegrationException {
        final HttpUrl httpUrl = createHttpUrl(url);
        return createMessageRequest(httpUrl, headers, jsonString);
    }

    public Request createMessageRequest(final HttpUrl httpUrl, final Map<String, String> headers, final String jsonString) throws IntegrationException {
        final RequestBody body = restConnection.createJsonRequestBody(jsonString);
        return restConnection.createPostRequest(httpUrl, headers, body);
    }

    public void sendMessageRequest(final Request request, final String messageType) throws IntegrationException {
        logger.info("Attempting to send a {} message...", messageType);
        final Response response = sendGenericRequest(request);
        if (response.isSuccessful()) {
            logger.info("Successfully sent a {} message!", messageType);
        }
    }

    public Response sendGenericRequest(final Request request) throws IntegrationException {
        Response response = null;
        try {
            response = restConnection.createResponse(request);
            logger.trace("Response: " + response.toString());
            return response;
        } catch (final IntegrationException integrationException) {
            throw integrationException;
        } catch (final Exception generalException) {
            throw new AlertException(generalException);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }
}
