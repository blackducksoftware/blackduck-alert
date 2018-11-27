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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestDistributionChannel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

import freemarker.template.TemplateException;

@Component(value = HipChatChannel.COMPONENT_NAME)
public class HipChatChannel extends RestDistributionChannel<HipChatGlobalConfigEntity, HipChatDistributionConfigEntity, HipChatChannelEvent> {
    public static final String COMPONENT_NAME = "channel_hipchat";
    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    public static final int MESSAGE_SIZE_LIMIT = 8000;
    private final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);

    @Autowired
    public HipChatChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility, final HipChatGlobalRepository hipChatGlobalRepository,
            final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(gson, alertProperties, blackDuckProperties, auditUtility, hipChatGlobalRepository, HipChatChannelEvent.class, channelRestConnectionFactory);
    }

    @Override
    public String getDistributionType() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public String getApiUrl(final HipChatGlobalConfigEntity globalConfig) {
        return getConfiguredApiUrl(globalConfig.getHostServer());
    }

    @Override
    public String testGlobalConfig(final TestConfigModel testConfig) throws IntegrationException {
        final Config restModel = testConfig.getRestModel();
        if (restModel == null) {
            throw new AlertException("The provided config was null.");
        }

        final HipChatGlobalConfig hipChatGlobalConfig = (HipChatGlobalConfig) restModel;
        final String configuredApiUrl = getConfiguredApiUrl(hipChatGlobalConfig.getHostServer());

        try (final RestConnection restConnection = getChannelRestConnectionFactory().createUnauthenticatedRestConnection(configuredApiUrl)) {
            final String testResult = testApiKeyAndApiUrlConnection(restConnection, configuredApiUrl, hipChatGlobalConfig.getApiKey());
            final Integer parsedRoomId;
            try {
                final String testRoomId = testConfig.getDestination().orElse(null);
                parsedRoomId = Integer.valueOf(testRoomId);
            } catch (final NumberFormatException e) {
                throw new AlertException("The provided room id is an invalid number.");
            }

            final HipChatChannelEvent event = new HipChatChannelEvent(null, null, null, null, null, parsedRoomId, Boolean.TRUE, "red");
            final String htmlMessage = "This is a test message sent by Alert.";
            final Request testRequest = createRequest(hipChatGlobalConfig.getHostServer(), hipChatGlobalConfig.getApiKey(), event, htmlMessage);
            sendMessageRequest(restConnection, testRequest, "test");
            return testResult;
        } catch (final IOException ex) {
            throw new AlertException("Connection error: see logs for more information.");
        }
    }

    @Override
    public List<Request> createRequests(final HipChatGlobalConfigEntity globalConfig, final HipChatChannelEvent event) throws IntegrationException {
        if (!isValidGlobalConfig(globalConfig)) {
            throw new AlertException("ERROR: Missing global config.");
        }
        if (event.getRoomId() == null) {
            throw new AlertException("Room ID missing");
        } else {
            final String htmlMessage = createHtmlMessage(event.getContent());
            if (isChunkedMessageNeeded(htmlMessage)) {
                return createChunkedRequestList(globalConfig, event, htmlMessage);
            } else {
                return Arrays.asList(createRequest(globalConfig.getHostServer(), globalConfig.getApiKey(), event, htmlMessage));
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
            queryParameters.put("auth_test", new HashSet<>(Arrays.asList("true")));

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", "Bearer " + apiKey);
            requestHeaders.put("Content-Type", "application/json");

            // TODO test if this string is still needed
            // The {"message":"test"} is required to avoid a BAD_REQUEST (OkHttp issue: #854)
            final Request request = createPostMessageRequest(url, requestHeaders, queryParameters, "{\"message\":\"test\"}");
            final Response response = sendGenericRequest(restConnection, request);
            if (200 <= response.getStatusCode() && response.getStatusCode() < 400) {
                return "API key is valid.";
            }
            throw new AlertException("Invalid API key: " + response.getStatusMessage());
        } catch (final IntegrationException e) {
            logger.error("Unable to create a response", e);
            throw new AlertException("Invalid API key: " + e.getMessage());
        }
    }

    private String getConfiguredApiUrl(final String configuredUrl) {
        if (StringUtils.isBlank(configuredUrl)) {
            return HIP_CHAT_API;
        }
        return configuredUrl.trim();
    }

    private boolean isValidGlobalConfig(final HipChatGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getApiKey());
    }

    private boolean isChunkedMessageNeeded(final String htmlMessage) {
        if (StringUtils.isNotBlank(htmlMessage)) {
            return htmlMessage.length() > MESSAGE_SIZE_LIMIT;
        } else {
            return false;
        }
    }

    private List<Request> createChunkedRequestList(final HipChatGlobalConfigEntity globalConfig, final HipChatChannelEvent event, final String htmlMessage) {
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
            final String contentTitle = String.format("%s (part %d of %d)<br/>", event.getProvider(), currentRequest, requestCount);
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
            requestList.add(createRequest(globalConfig.getHostServer(), globalConfig.getApiKey(), event, contentTitle + content));
            currentRequest++;
        }

        return requestList;
    }

    private Request createRequest(final String hostServer, final String apiKey, final HipChatChannelEvent event, final String htmlMessage) {
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, event.getNotify(), event.getColor());

        final String url = getConfiguredApiUrl(hostServer) + "/v2/room/" + event.getRoomId().toString() + "/notification";

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
