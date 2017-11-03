/**
 * Copyright (C) 2017 Black Duck Software, Inc.
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
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.UserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.ChannelUserRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class HipChatChannel extends DistributionChannel<HipChatEvent, HipChatConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannel.class);

    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    private final HipChatRepository hipChatRepository;
    private final ChannelUserRepository<UserHipChatRelation> userRelationRepository;

    @Autowired
    public HipChatChannel(final Gson gson, final ChannelUserRepository<UserHipChatRelation> userRelationRepository, final HipChatRepository hipChatRepository) {
        super(gson, HipChatEvent.class);
        this.hipChatRepository = hipChatRepository;
        this.userRelationRepository = userRelationRepository;
    }

    @JmsListener(destination = SupportedChannels.HIPCHAT)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

    @Override
    public void handleEvent(final HipChatEvent event) {
        final UserHipChatRelation relationRow = userRelationRepository.findChannelConfig(event.getUserConfigId());
        final Long configId = relationRow.getChannelConfigId();
        final HipChatConfigEntity configuration = hipChatRepository.findOne(configId);
        sendMessage(event, configuration);
    }

    @Override
    public void sendMessage(final HipChatEvent event, final HipChatConfigEntity config) {
        final String htmlMessage = createHtmlMessage(event.getProjectData());
        try {
            sendMessage(config, HIP_CHAT_API, htmlMessage, AlertConstants.ALERT_APPLICATION_NAME);
        } catch (final IntegrationRestException e) {
            logger.error(e.getHttpStatusCode() + ":" + e.getHttpStatusMessage());
            logger.error(e.getMessage(), e);
        }
    }

    private String sendMessage(final HipChatConfigEntity config, final String apiUrl, final String message, final String senderName) throws IntegrationRestException {
        final RestConnection connection = ChannelRestConnectionFactory.createUnauthenticatedRestConnection(apiUrl);
        if (connection != null) {
            final String jsonString = getJsonString(message, senderName, config.getNotify(), config.getColor());
            final RequestBody body = connection.createJsonRequestBody(jsonString);

            final List<String> urlSegments = Arrays.asList("v2", "room", config.getRoomId().toString(), "notification");
            final HttpUrl httpUrl = connection.createHttpUrl(urlSegments);

            final Map<String, String> map = new HashMap<>();
            map.put("Authorization", "Bearer " + config.getApiKey());
            map.put("Content-Type", "application/json");

            final Request request = connection.createPostRequest(httpUrl, map, body);
            try {
                logger.info("Attempting to send a HipChat message...");
                final Response response = connection.handleExecuteClientCall(request);
                logger.info("Successfully sent a HipChat message!");
                if (logger.isTraceEnabled()) {
                    logger.trace("Response: " + response.toString());
                }
                return "Attempting to send a test message.";
            } catch (final IntegrationException e) {
                throw new IntegrationRestException(HttpStatus.BAD_REQUEST.value(), "Failed to send a HipChat message", e.getMessage(), e);
            }
        } else {
            throw new IntegrationRestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "No message will be sent because a connection was not established.", "No message will be sent because a connection was not established.");
        }
    }

    @Override
    public String testMessage(final HipChatConfigEntity config) throws IntegrationException {
        return sendMessage(config, HIP_CHAT_API, "Test Message", AlertConstants.ALERT_APPLICATION_NAME + " Tester");
    }

    private String createHtmlMessage(final ProjectData projectData) {
        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<strong>" + projectData.getProjectName() + " > " + projectData.getProjectVersion() + "</strong>");

        final Map<NotificationCategoryEnum, CategoryData> categoryMap = projectData.getCategoryMap();
        if (categoryMap != null) {
            for (final NotificationCategoryEnum category : NotificationCategoryEnum.values()) {
                final CategoryData data = categoryMap.get(category);
                if (data != null) {
                    htmlBuilder.append("<br />- - - - - - - - - - - - - - - - - - - -");
                    htmlBuilder.append("<br />Type: " + data.getCategoryKey());
                    htmlBuilder.append("<br />Number of Changes: " + data.getItemCount());
                    for (final ItemData item : data.getItemList()) {
                        final Map<String, Object> dataSet = item.getDataSet();
                        htmlBuilder.append("<p>  Rule: " + dataSet.get(ItemTypeEnum.RULE.toString()));
                        htmlBuilder.append(" | Component: " + dataSet.get(ItemTypeEnum.COMPONENT.toString()));
                        htmlBuilder.append(" [" + dataSet.get(ItemTypeEnum.VERSION.toString()) + "]</p>");
                    }
                }
            }
        } else {
            htmlBuilder.append("<br /><i>A notification was received, but it was empty.</i>");
        }
        return htmlBuilder.toString();
    }

    private String getJsonString(final String htmlMessage, final String from, final boolean notify, final String color) {
        final JsonObject json = new JsonObject();
        json.addProperty("message_format", "html");
        json.addProperty("message", htmlMessage);
        json.addProperty("from", from);
        json.addProperty("notify", notify);
        json.addProperty("color", color);

        return json.toString();
    }

}
