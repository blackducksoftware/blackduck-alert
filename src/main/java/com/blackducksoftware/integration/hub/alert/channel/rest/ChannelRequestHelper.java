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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
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

    public Request createMessageRequest(final List<String> urlSegments, final Map<String, String> headers, final String jsonString) {
        final HttpUrl httpUrl = restConnection.createHttpUrl(urlSegments);
        return createMessageRequest(httpUrl, headers, jsonString);
    }

    public Request createMessageRequest(final String url, final Map<String, String> headers, final String jsonString) {
        final HttpUrl httpUrl = restConnection.createHttpUrl(url);
        return createMessageRequest(httpUrl, headers, jsonString);
    }

    public Request createMessageRequest(final HttpUrl httpUrl, final Map<String, String> headers, final String jsonString) {
        final RequestBody body = restConnection.createJsonRequestBody(jsonString);
        return restConnection.createPostRequest(httpUrl, headers, body);
    }

    public void sendMessageRequest(final Request request) throws IntegrationException {
        sendMessageRequest(request, "channel");
    }

    public void sendMessageRequest(final Request request, final String messageType) throws IntegrationException {
        logger.info("Attempting to send a {} message...", messageType);
        final Response response = sendGenericRequest(request);
        if (response.isSuccessful()) {
            logger.info("Successfully sent a {} message!", messageType);
        }
    }

    public Response sendGenericRequest(final Request request) throws IntegrationException {
        final Response response = restConnection.createResponse(request);
        if (logger.isTraceEnabled()) {
            logger.trace("Response: " + response.toString());
        }
        response.body().close();
        return response;
    }
}
