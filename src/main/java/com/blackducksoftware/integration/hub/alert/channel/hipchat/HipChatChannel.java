/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.alert.channel.ChannelFreemarkerTemplatingService;
import com.blackducksoftware.integration.hub.alert.channel.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import freemarker.template.TemplateException;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class HipChatChannel extends DistributionChannel<HipChatEvent, GlobalHipChatConfigEntity, HipChatDistributionConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannel.class);

    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    private final GlobalProperties globalProperties;

    @Autowired
    public HipChatChannel(final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final GlobalProperties globalProperties, final GlobalHipChatRepositoryWrapper globalHipChatRepository,
            final CommonDistributionRepositoryWrapper commonDistributionRepository, final HipChatDistributionRepositoryWrapper hipChatDistributionRepository) {
        super(gson, auditEntryRepository, globalHipChatRepository, hipChatDistributionRepository, commonDistributionRepository, HipChatEvent.class);
        this.globalProperties = globalProperties;
    }

    @JmsListener(destination = SupportedChannels.HIPCHAT)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

    @Override
    public void sendMessage(final HipChatEvent event, final HipChatDistributionConfigEntity config) {
        final String htmlMessage = createHtmlMessage(event.getProjectData());
        try {
            sendMessage(config, HIP_CHAT_API, htmlMessage, AlertConstants.ALERT_APPLICATION_NAME);
            setAuditEntrySuccess(event.getAuditEntryId());
        } catch (final IntegrationException e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);

            if (e instanceof IntegrationRestException) {
                logger.error(((IntegrationRestException) e).getHttpStatusCode() + ":" + ((IntegrationRestException) e).getHttpStatusMessage());
            }
            logger.error(e.getMessage(), e);
        } catch (final Exception e) {
            setAuditEntryFailure(event.getAuditEntryId(), e.getMessage(), e);
            logger.error(e.getMessage(), e);
        }
    }

    private String sendMessage(final HipChatDistributionConfigEntity config, final String apiUrl, final String message, final String senderName) throws IntegrationException {
        final ChannelRestConnectionFactory restConnectionFactory = new ChannelRestConnectionFactory(globalProperties);
        final RestConnection connection = restConnectionFactory.createUnauthenticatedRestConnection(apiUrl);
        if (connection != null) {
            final String jsonString = getJsonString(message, senderName, config.getNotify(), config.getColor());
            final RequestBody body = connection.createJsonRequestBody(jsonString);

            final List<String> urlSegments = Arrays.asList("v2", "room", config.getRoomId().toString(), "notification");
            final HttpUrl httpUrl = connection.createHttpUrl(urlSegments);

            final Map<String, String> map = new HashMap<>();
            map.put("Authorization", "Bearer " + getGlobalConfigEntity().getApiKey());
            map.put("Content-Type", "application/json");

            final Request request = connection.createPostRequest(httpUrl, map, body);
            try {
                logger.info("Attempting to send a HipChat message...");
                final Response response = connection.createResponse(request);
                logger.info("Successfully sent a HipChat message!");
                if (logger.isTraceEnabled()) {
                    logger.trace("Response: " + response.toString());
                }
                return Integer.toString(response.code());
            } catch (final IntegrationException e) {
                throw new IntegrationRestException(HttpStatus.BAD_REQUEST.value(), "Failed to send a HipChat message", e.getMessage(), e);
            }
        } else {
            throw new IntegrationRestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "No message will be sent because a connection was not established.", "No message will be sent because a connection was not established.");
        }
    }

    private String createHtmlMessage(final ProjectData projectData) {
        try {
            // TODO determine the actual template location for deployment
            final ChannelFreemarkerTemplatingService freemarkerTemplatingService = new ChannelFreemarkerTemplatingService(System.getProperties().getProperty("user.dir") + "/src/main/resources/hipchat/templates");

            final HashMap<String, Object> model = new HashMap<>();
            model.put("projectName", projectData.getProjectName());
            model.put("projectVersion", projectData.getProjectVersion());
            model.put("categoryMap", projectData.getCategoryMap());

            return freemarkerTemplatingService.getResolvedTemplate(model, "notification.ftl");
        } catch (final IOException | TemplateException e) {
            throw new RuntimeException(e);
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
