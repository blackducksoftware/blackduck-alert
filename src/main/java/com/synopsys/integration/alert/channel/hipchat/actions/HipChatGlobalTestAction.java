/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.hipchat.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

@Component
public class HipChatGlobalTestAction extends TestAction {
    private final Logger logger = LoggerFactory.getLogger(HipChatGlobalTestAction.class);

    private final HipChatChannel hipChatChannel;
    private final RestChannelUtility restChannelUtility;

    @Autowired
    public HipChatGlobalTestAction(final HipChatChannel hipChatChannel, final RestChannelUtility restChannelUtility) {
        this.hipChatChannel = hipChatChannel;
        this.restChannelUtility = restChannelUtility;
    }

    @Override
    public String testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
        final String apiKey = fieldAccessor.getString(HipChatDescriptor.KEY_API_KEY).orElseThrow(() -> new AlertException("ERROR: Missing API key in the global HipChat config."));
        final String configuredApiUrl = fieldAccessor.getString(HipChatDescriptor.KEY_HOST_SERVER).orElseThrow(() -> new AlertException("ERROR: Missing the server URL in the global HipChat config."));
        final Integer parsedRoomId;
        try {
            final String testRoomId = testConfig.getDestination().orElse(null);
            parsedRoomId = Integer.valueOf(testRoomId);
        } catch (final NumberFormatException e) {
            throw new AlertException("The provided room id is an invalid number.");
        }

        final IntHttpClient intHttpClient = restChannelUtility.getIntHttpClient();
        testApiKeyAndApiUrlConnection(intHttpClient, configuredApiUrl, apiKey);

        final String htmlMessage = "This is a test message sent by Alert.";
        final Request testRequest = hipChatChannel.createRequest(configuredApiUrl, apiKey, parsedRoomId, Boolean.TRUE, "red", htmlMessage);
        restChannelUtility.sendMessageRequest(intHttpClient, testRequest, "test");
        return "Successfully tested HipChat server";
    }

    private void testApiKeyAndApiUrlConnection(final IntHttpClient intHttpClient, final String configuredApiUrl, final String apiKey) throws IntegrationException {
        if (StringUtils.isBlank(apiKey)) {
            throw new AlertException("Invalid API key: API key not provided");
        }
        if (StringUtils.isBlank(configuredApiUrl)) {
            throw new AlertException("Invalid server URL: server URL not provided");
        }
        if (intHttpClient == null) {
            throw new AlertException("Connection error: see logs for more information.");
        }
        try {
            sendTestRequest(intHttpClient, configuredApiUrl, apiKey);
        } catch (final IntegrationException integrationException) {
            logger.error("Unable to create a response", integrationException);
            throw new AlertException("Invalid HipChat configuration.");
        }
    }

    private void sendTestRequest(final IntHttpClient intHttpClient, final String configuredApiUrl, final String apiKey) throws IntegrationException {
        final String url = configuredApiUrl + "/v2/room/*/notification";
        final Map<String, Set<String>> queryParameters = new HashMap<>();
        queryParameters.put("auth_test", new HashSet<>(Collections.singleton("true")));

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + apiKey);
        requestHeaders.put("Content-Type", "application/json");

        final Request request = restChannelUtility.createPostMessageRequest(url, requestHeaders, queryParameters);
        try (final Response response = restChannelUtility.sendGenericRequest(intHttpClient, request)) {
            if (RestConstants.OK_200 > response.getStatusCode() || response.getStatusCode() >= RestConstants.BAD_REQUEST_400) {
                throw new AlertException("Invalid API key: " + response.getStatusMessage());
            }
        } catch (final IOException ioException) {
            throw new AlertException(ioException.getMessage(), ioException);
        }
    }

}
