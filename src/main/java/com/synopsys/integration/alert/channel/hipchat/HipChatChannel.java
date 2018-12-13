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
package com.synopsys.integration.alert.channel.hipchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.AlertConstants;
import com.synopsys.integration.alert.channel.ChannelFreemarkerTemplatingService;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDistributionUIConfig;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatGlobalUIConfig;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestDistributionChannel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

import freemarker.template.TemplateException;

@Component(value = HipChatChannel.COMPONENT_NAME)
public class HipChatChannel extends RestDistributionChannel {
    public static final String COMPONENT_NAME = "channel_hipchat";
    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    public static final int MESSAGE_SIZE_LIMIT = 8000;
    private final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);

    @Autowired
    public HipChatChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility,
            final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(HipChatChannel.COMPONENT_NAME, gson, alertProperties, blackDuckProperties, auditUtility, channelRestConnectionFactory);
    }

    @Override
    public String getApiUrl(final DistributionEvent distributionEvent) {
        final FieldAccessor fieldAccessor = distributionEvent.getFieldAccessor();
        final Optional<String> hostServer = fieldAccessor.getString(HipChatGlobalUIConfig.KEY_HOST_SERVER);
        return hostServer.orElse(HIP_CHAT_API);
    }

    // TODO move channel global testing to descriptorActionApi. Goal is to only define how to send data here. Testing methods will insert appropriate values for testing
    public String testGlobalConfig(final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfig.getFieldModel().convertToFieldAccessor();
        final Optional<String> apiKey = fieldAccessor.getString(HipChatGlobalUIConfig.KEY_API_KEY);
        final String configuredApiUrl = fieldAccessor.getString(HipChatGlobalUIConfig.KEY_HOST_SERVER).orElse(HIP_CHAT_API);

        if (!apiKey.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        final RestConnection restConnection = getChannelRestConnectionFactory().createRestConnection();
        final String testResult = testApiKeyAndApiUrlConnection(restConnection, configuredApiUrl, apiKey.get());
        final Integer parsedRoomId;
        try {
            final String testRoomId = testConfig.getDestination().orElse(null);
            parsedRoomId = Integer.valueOf(testRoomId);
        } catch (final NumberFormatException e) {
            throw new AlertException("The provided room id is an invalid number.");
        }

        final String htmlMessage = "This is a test message sent by Alert.";
        final Request testRequest = createRequest(configuredApiUrl, apiKey.get(), parsedRoomId, Boolean.TRUE, "red", htmlMessage);
        sendMessageRequest(restConnection, testRequest, "test");
        return testResult;
    }

    @Override
    public List<Request> createRequests(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fieldAccessor = event.getFieldAccessor();
        final Optional<String> apiKey = fieldAccessor.getString(HipChatGlobalUIConfig.KEY_API_KEY);
        final String hostServer = fieldAccessor.getString(HipChatGlobalUIConfig.KEY_HOST_SERVER).orElse(HIP_CHAT_API);

        if (!apiKey.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        final Optional<Integer> roomId = fieldAccessor.getInteger(HipChatDistributionUIConfig.KEY_ROOM_ID);
        final Boolean notify = fieldAccessor.getBoolean(HipChatDistributionUIConfig.KEY_NOTIFY).orElse(false);
        final String color = fieldAccessor.getString(HipChatDistributionUIConfig.KEY_COLOR).orElse("Red");

        if (!roomId.isPresent()) {
            throw new AlertException("Room ID missing");
        } else {
            final String htmlMessage = createHtmlMessage(event.getContent());
            if (isChunkedMessageNeeded(htmlMessage)) {
                return createChunkedRequestList(hostServer, apiKey.get(), roomId.get(), notify, color, event.getProvider(), htmlMessage);
            } else {
                return Arrays.asList(createRequest(hostServer, apiKey.get(), roomId.get(), notify, color, htmlMessage));
            }
        }
    }

    private String testApiKeyAndApiUrlConnection(final RestConnection restConnection, final String configuredApiUrl, final String apiKey) throws IntegrationException {
        if (StringUtils.isBlank(apiKey)) {
            throw new AlertException("Invalid API key: API key not provided");
        }
        if (restConnection == null) {
            throw new AlertException("Connection error: see logs for more information.");
        }
        try {
            final String url = configuredApiUrl + "/v2/room/*/notification";
            final Map<String, Set<String>> queryParameters = new HashMap<>();
            queryParameters.put("auth_test", new HashSet<>(Collections.singleton("true")));

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", "Bearer " + apiKey);
            requestHeaders.put("Content-Type", "application/json");

            final Request request = createPostMessageRequest(url, requestHeaders, queryParameters);
            try (final Response response = sendGenericRequest(restConnection, request)) {
                if (RestConstants.OK_200 <= response.getStatusCode() && response.getStatusCode() < RestConstants.BAD_REQUEST_400) {
                    return "API key is valid.";
                }
                throw new AlertException("Invalid API key: " + response.getStatusMessage());
            } catch (final IOException ioException) {
                throw new AlertException(ioException.getMessage(), ioException);
            }
        } catch (final IntegrationException integrationException) {
            logger.error("Unable to create a response", integrationException);
            throw new AlertException("Invalid API key: " + integrationException.getMessage());
        }
    }

    private boolean isValidGlobalConfig(final String apiKey) {
        return StringUtils.isNotBlank(apiKey);
    }

    private boolean isChunkedMessageNeeded(final String htmlMessage) {
        if (StringUtils.isNotBlank(htmlMessage)) {
            return htmlMessage.length() > MESSAGE_SIZE_LIMIT;
        } else {
            return false;
        }
    }

    private List<Request> createChunkedRequestList(final String hostServer, final String apiKey, final Integer roomId, final Boolean notify, final String color, final String provider, final String htmlMessage) {
        final int contentLength = htmlMessage.length();
        logger.info("Message too large.  Creating chunks...");
        logger.info("Content length: {}", contentLength);
        final int additionPage = (contentLength % MESSAGE_SIZE_LIMIT == 0) ? 0 : 1;
        final int requestCount = (contentLength / MESSAGE_SIZE_LIMIT) + additionPage;
        logger.info("Number of requests to submit: {}", requestCount);
        final List<Request> requestList = new ArrayList<>(requestCount);
        int end = 0;
        int currentRequest = 1;
        while (end < contentLength) {
            logger.info("Creating request {} of {}", currentRequest, requestCount);
            final String contentTitle = String.format("%s (part %d of %d)<br/>", provider, currentRequest, requestCount);
            final int start = end;
            end = end + MESSAGE_SIZE_LIMIT;
            final String content;
            if (end > htmlMessage.length()) {
                content = htmlMessage.substring(start);
            } else {
                final String arbitrarilySplitMessage = htmlMessage.substring(start, end);
                final String lineBreak = "<br/>";
                final int lastBreak = arbitrarilySplitMessage.lastIndexOf("<br/>");
                if (lastBreak > 0) {
                    end = start + lastBreak + lineBreak.length();
                }
                content = htmlMessage.substring(start, end);
            }
            requestList.add(createRequest(hostServer, apiKey, roomId, notify, color, contentTitle + content));
            currentRequest++;
        }

        return requestList;
    }

    private Request createRequest(final String hostServer, final String apiKey, final Integer roomId, final Boolean notify, final String color, final String htmlMessage) {
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, notify, color);

        final String url = hostServer + "/v2/room/" + roomId + "/notification";

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + apiKey);
        requestHeaders.put("Content-Type", "application/json");

        return createPostMessageRequest(url, requestHeaders, jsonString);
    }

    private String createHtmlMessage(final AggregateMessageContent messageContent) throws AlertException {
        try {
            final String templatesDirectory = getAlertProperties().getAlertTemplatesDir();
            final String templateDirectoryPath;
            if (StringUtils.isNotBlank(templatesDirectory)) {
                templateDirectoryPath = templatesDirectory + "/hipchat";
            } else {
                templateDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/hipchat/templates";
            }
            final ChannelFreemarkerTemplatingService freemarkerTemplatingService = new ChannelFreemarkerTemplatingService(templateDirectoryPath);

            final HashMap<String, Object> model = new HashMap<>();
            model.put("content", messageContent);

            return freemarkerTemplatingService.getResolvedTemplate(model, "message_content.ftl");
        } catch (final IOException | TemplateException e) {
            throw new AlertException(e);
        }
    }

    private String getJsonString(final String htmlMessage, final String from, final boolean notify, final String color) {
        final JsonObject json = new JsonObject();
        json.addProperty("message_format", "html");
        if (htmlMessage != null) {
            json.addProperty("message", htmlMessage);
        }
        if (from != null) {
            json.addProperty("from", from);
        }
        json.addProperty("notify", notify);
        if (color != null) {
            json.addProperty("color", color);
        }
        return json.toString();
    }

}
