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
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestDistributionChannel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.synopsys.integration.alert.web.model.Config;
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
    public HipChatChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final HipChatGlobalRepository hipChatGlobalRepository,
        final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(gson, alertProperties, blackDuckProperties, auditEntryRepository, hipChatGlobalRepository, HipChatChannelEvent.class, channelRestConnectionFactory);
    }

    @Override
    public String getDistributionType() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public String getApiUrl(final HipChatGlobalConfigEntity globalConfig) {
        return getApiUrl(globalConfig.getHostServer());
    }

    @Override
    public String getApiUrl(final String apiUrl) {
        String hipChatHostServer = HIP_CHAT_API;
        if (!StringUtils.isBlank(apiUrl)) {
            hipChatHostServer = apiUrl;
        }
        return hipChatHostServer;
    }

    @Override
    public String testGlobalConfig(final Config restModel) throws IntegrationException {
        if (restModel == null) {
            return "The provided config was null.";
        }
        final HipChatGlobalConfig hipChatGlobalConfig = (HipChatGlobalConfig) restModel;

        if (StringUtils.isBlank(hipChatGlobalConfig.getApiKey())) {
            throw new IntegrationException("Invalid API key: API key not provided");
        }
        final RestConnection restConnection = getChannelRestConnectionFactory().createUnauthenticatedRestConnection(getApiUrl(hipChatGlobalConfig.getHostServer()));
        if (restConnection != null) {
            try {
                final String url = getApiUrl(hipChatGlobalConfig.getHostServer()) + "/v2/room/*/notification";
                final Map<String, Set<String>> queryParameters = new HashMap<>();
                queryParameters.put("auth_test", new HashSet<>(Arrays.asList("true")));

                final Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("Authorization", "Bearer " + hipChatGlobalConfig.getApiKey());
                requestHeaders.put("Content-Type", "application/json");

                // The {"message":"test"} is required to avoid a BAD_REQUEST (OkHttp issue: #854)
                final Request request = createPostMessageRequest(url, requestHeaders, queryParameters, "{\"message\":\"test\"}");

                final Response response = sendGenericRequest(restConnection, request);
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
                    return "API key is valid.";
                }
                return "Invalid API key: " + response.getStatusMessage();
            } catch (final IntegrationException e) {
                logger.error("Unable to create a response", e);
                throw new IntegrationException("Invalid API key: " + e.getMessage());
            } finally {
                try {
                    restConnection.close();
                } catch (final IOException ex) {
                    // close the connection quietly
                }
            }
        }
        return "Connection error: see logs for more information.";
    }

    @Override
    public List<Request> createRequests(final HipChatGlobalConfigEntity globalConfig, final HipChatChannelEvent event)
        throws IntegrationException {
        if (!isValidGlobalConfig(globalConfig)) {
            throw new IntegrationException("ERROR: Missing global config.");
        }
        if (event.getRoomId() == null) {
            throw new IntegrationException("Room ID missing");
        } else {
            if (isChunkedMessageNeeded(event)) {
                return createChunkedRequestList(globalConfig, event);
            } else {
                final String contentTitle = event.getProvider();
                return Arrays.asList(createRequest(globalConfig, event, contentTitle, event.getContent().toString()));
            }
        }
    }

    private boolean isValidGlobalConfig(final HipChatGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getApiKey());
    }

    private boolean isChunkedMessageNeeded(final ChannelEvent event) {
        //TODO fix the calculation of pages for the event content.
        final AggregateMessageContent eventContent = event.getContent();
        final String contentString = eventContent.toString();
        if (StringUtils.isNotBlank(eventContent.toString())) {
            return contentString.length() > MESSAGE_SIZE_LIMIT;
        } else {
            return false;
        }
    }

    private List<Request> createChunkedRequestList(final HipChatGlobalConfigEntity globalConfig, final HipChatChannelEvent event)
        throws IntegrationException {
        final AggregateMessageContent eventContent = event.getContent();
        final String contentString = eventContent.toString();
        final int contentLength = contentString.length();
        logger.info("Message too large.  Creating chunks...");
        logger.info("Content length: {}", contentLength);
        final int additionPage = (contentLength % MESSAGE_SIZE_LIMIT == 0) ? 0 : 1;
        final int requestCount = (contentLength / MESSAGE_SIZE_LIMIT) + additionPage;
        logger.info("Number of requests to submit: {}", requestCount);
        final List<Request> requestList = new ArrayList<>(requestCount);
        int end = 0;
        int currentRequest = 1;
        while (end < contentString.length()) {
            logger.info("Creating request {} of {}", currentRequest, requestCount);
            final String contentTitle = String.format("%s (part %d of %d)", event.getProvider(), currentRequest, requestCount);
            final int start = end;
            end = end + MESSAGE_SIZE_LIMIT;
            final String content;
            if (end > contentString.length()) {
                content = contentString.substring(start);
            } else {
                content = contentString.substring(start, end);
            }
            requestList.add(createRequest(globalConfig, event, contentTitle, content));
            currentRequest++;
        }

        return requestList;
    }

    private Request createRequest(final HipChatGlobalConfigEntity globalConfig, final HipChatChannelEvent event, final String contentTitle, final String content)
        throws IntegrationException {
        final String htmlMessage = createHtmlMessage(contentTitle, content);
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, event.getNotify(), event.getColor());

        final String url = getApiUrl(globalConfig.getHostServer()) + "/v2/room/" + event.getRoomId().toString() + "/notification";

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + globalConfig.getApiKey());
        requestHeaders.put("Content-Type", "application/json");

        return createPostMessageRequest(url, requestHeaders, jsonString);
    }

    private String createHtmlMessage(final String contentTitle, final String content) throws AlertException {
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
            model.put("content", content);
            model.put("contentTitle", contentTitle);

            return freemarkerTemplatingService.getResolvedTemplate(model, "audit.ftl");
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
