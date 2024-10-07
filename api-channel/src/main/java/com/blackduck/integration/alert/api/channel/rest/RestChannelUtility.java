/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.rest.body.BodyContentConverter;
import com.blackduck.integration.rest.body.StringBodyContent;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;

public class RestChannelUtility {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IntHttpClient intHttpClient;

    public RestChannelUtility(IntHttpClient intHttpClient) {
        this.intHttpClient = intHttpClient;
    }

    public void sendMessage(List<Request> requests, String eventDestination) throws AlertException {
        for (Request request : requests) {
            sendMessageRequest(request, eventDestination);
        }
    }

    public Request createPostMessageRequest(String url, Map<String, String> headers, String jsonString) {
        return createPostMessageRequest(url, headers, null, jsonString);
    }

    public Request createPostMessageRequest(String url, Map<String, String> headers, @Nullable Map<String, Set<String>> queryParameters, String jsonString) {
        HttpUrl httpUrl;
        try {
            httpUrl = new HttpUrl(url);
        } catch (IntegrationException e) {
            throw new AlertRuntimeException(e);
        }

        Request.Builder requestBuilder = new Request.Builder()
            .method(HttpMethod.POST)
            .url(httpUrl);
        requestBuilder.getHeaders().putAll(headers);
        requestBuilder.bodyContent(new StringBodyContent(jsonString, BodyContentConverter.DEFAULT));

        if (queryParameters != null && !queryParameters.isEmpty()) {
            requestBuilder.queryParameters(queryParameters);
        }
        return requestBuilder.build();
    }

    private void sendMessageRequest(Request request, String messageType) throws AlertException {
        logger.info("Attempting to send a {} message...", messageType);
        try (Response response = sendGenericRequest(request)) {
            if (RestConstants.OK_200 >= response.getStatusCode() && response.getStatusCode() < RestConstants.MULT_CHOICE_300) {
                logger.info("Successfully sent a {} message!", messageType);
            } else {
                throw new AlertException(String.format("Could not send message: %s. Status code: %s", response.getStatusMessage(), response.getStatusCode()));
            }
        } catch (Exception e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    private Response sendGenericRequest(Request request) throws IntegrationException {
        Response response = intHttpClient.execute(request);
        logger.trace("Response: {}", response);
        return response;
    }

}
