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
package com.blackducksoftware.integration.alert.channel.hipchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.AlertConstants;
import com.blackducksoftware.integration.alert.channel.ChannelFreemarkerTemplatingService;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.alert.common.AlertProperties;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import freemarker.template.TemplateException;

@Component(value = HipChatChannel.COMPONENT_NAME)
@Transactional
public class HipChatChannel extends RestDistributionChannel<HipChatGlobalConfigEntity, HipChatDistributionConfigEntity> {
    private final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    public static final String COMPONENT_NAME = "channel_hipchat";
    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    public static final int MESSAGE_SIZE_LIMIT = 8000;

    @Autowired
    public HipChatChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final HipChatGlobalRepository hipChatGlobalRepository,
            final CommonDistributionRepository commonDistributionRepository,
            final HipChatDistributionRepository hipChatDistributionRepository, final ChannelRestConnectionFactory channelRestConnectionFactory, final ContentConverter contentExtractor) {
        super(gson, alertProperties, blackDuckProperties, auditEntryRepository, hipChatGlobalRepository, hipChatDistributionRepository, commonDistributionRepository, channelRestConnectionFactory, contentExtractor);
    }

    @Override
    public String getApiUrl(final HipChatGlobalConfigEntity globalConfig) {
        String hipChatHostServer = HIP_CHAT_API;
        final String customHostServer = globalConfig.getHostServer();
        if (!StringUtils.isBlank(customHostServer)) {
            hipChatHostServer = customHostServer;
        }
        return hipChatHostServer;
    }

    @Override
    public String testGlobalConfig(final HipChatGlobalConfigEntity entity) throws IntegrationException {
        if (entity == null) {
            return "The provided entity was null.";
        }
        if (StringUtils.isBlank(entity.getApiKey())) {
            throw new IntegrationException("Invalid API key: API key not provided");
        }
        final RestConnection restConnection = getChannelRestConnectionFactory().createUnauthenticatedRestConnection(getApiUrl(entity));
        if (restConnection != null) {
            try {
                final String url = getApiUrl(entity) + "/v2/room/*/notification";
                final Map<String, String> queryParameters = new HashMap<>();
                queryParameters.put("auth_test", "true");

                final Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("Authorization", "Bearer " + entity.getApiKey());
                requestHeaders.put("Content-Type", "application/json");

                // The {"message":"test"} is required to avoid a BAD_REQUEST (OkHttp issue: #854)
                final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
                final Request request = channelRequestHelper.createPostMessageRequest(url, requestHeaders, queryParameters, "{\"message\":\"test\"}");

                final Response response = channelRequestHelper.sendGenericRequest(request);
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
                    return "API key is valid.";
                }
                return "Invalid API key: " + response.getStatusMessage();
            } catch (final IntegrationException e) {
                restConnection.logger.error("Unable to create a response", e);
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
    public List<Request> createRequests(final ChannelRequestHelper channelRequestHelper, final HipChatDistributionConfigEntity config, final HipChatGlobalConfigEntity globalConfig, final ChannelEvent event)
            throws IntegrationException {
        if (config.getRoomId() == null) {
            throw new IntegrationException("Room ID missing");
        } else {
            if (isChunkedMessageNeeded(event)) {
                final String eventContent = event.getContent();
                final int requestCount = calculateChunks(eventContent);
                final List<Request> requestList = createChunkedRequestList(channelRequestHelper, config, globalConfig, event, requestCount);
                return requestList;
            } else {
                final String contentTitle = String.format("%s -> %s", event.getProvider(), event.getNotificationType());
                return Arrays.asList(createRequest(channelRequestHelper, config, globalConfig, contentTitle, event.getContent()));
            }
        }
    }

    private boolean isChunkedMessageNeeded(final ChannelEvent event) {
        final String eventContent = event.getContent();
        if (StringUtils.isNotBlank(eventContent)) {
            return eventContent.length() > MESSAGE_SIZE_LIMIT;
        } else {
            return false;
        }
    }

    private int calculateChunks(final String eventContent) {
        final int contentLength = eventContent.length();
        logger.info("Message too large.  Creating chunks..");
        logger.info("Content length: {}", contentLength);
        final int additionPage = (contentLength % MESSAGE_SIZE_LIMIT == 0) ? 0 : 1;
        final int requestCount = (contentLength / MESSAGE_SIZE_LIMIT) + additionPage;
        logger.info("Number of requests to submit: {}", requestCount);
        return requestCount;
    }

    private List<Request> createChunkedRequestList(final ChannelRequestHelper channelRequestHelper, final HipChatDistributionConfigEntity config, final HipChatGlobalConfigEntity globalConfig, final ChannelEvent event,
            final int requestCount) throws IntegrationException {
        final List<Request> requestList = new ArrayList<>(requestCount);
        final String eventContent = event.getContent();
        for (int index = 0; index < requestCount; index++) {
            final int currentRequest = index + 1;
            logger.info("Creating request {} of {}", currentRequest, requestCount);
            final String contentTitle = String.format("%s -> %s (part %d of %d)", event.getProvider(), event.getNotificationType(), currentRequest, requestCount);
            final int start = MESSAGE_SIZE_LIMIT * index;
            final int end = start + MESSAGE_SIZE_LIMIT;
            final String content;
            if (index == requestCount - 1) {
                content = eventContent.substring(start);
            } else {
                content = eventContent.substring(start, end);
            }
            requestList.add(createRequest(channelRequestHelper, config, globalConfig, contentTitle, content));
        }

        return requestList;
    }

    private Request createRequest(final ChannelRequestHelper channelRequestHelper, final HipChatDistributionConfigEntity config, final HipChatGlobalConfigEntity globalConfig, final String contentTitle, final String content)
            throws IntegrationException {
        final String htmlMessage = createHtmlMessage(contentTitle, content);
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, config.getNotify(), config.getColor());

        final String url = getApiUrl(globalConfig) + "/v2/room/" + config.getRoomId().toString() + "/notification";

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + globalConfig.getApiKey());
        requestHeaders.put("Content-Type", "application/json");

        return channelRequestHelper.createPostMessageRequest(url, requestHeaders, jsonString);
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
