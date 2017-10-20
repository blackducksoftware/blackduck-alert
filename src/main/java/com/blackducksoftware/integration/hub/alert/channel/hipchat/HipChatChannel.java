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
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class HipChatChannel extends DistributionChannel<String> {
    public static final String HIP_CHAT_API = "https://api.hipchat.com";
    public static final String HIP_CHAT_FROM_NAME = "Hub Alert";

    private static final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    private final Gson gson;
    private final HipChatRepository hipChatRepository;

    @Autowired
    public HipChatChannel(final Gson gson, final HipChatRepository hipChatRepository) {
        this.gson = gson;
        this.hipChatRepository = hipChatRepository;
    }

    @JmsListener(destination = SupportedChannels.HIPCHAT)
    @Override
    public void recieveMessage(final String message) {
        logger.info("Received hipchat event message: {}", message);
        final HipChatEvent event = gson.fromJson(message, HipChatEvent.class);
        logger.info("HipChat event {}", event);

        handleEvent(event);
    }

    public void handleEvent(final HipChatEvent event) {
        final String notificationMessage = event.getNotificationEntity().toString();
        final JsonObject card = formatNotificationEntity(event.getNotificationEntity());

        // TODO only read from the DB (i.e. remove this)
        final HipChatConfigEntity entity = hipChatRepository.save(new HipChatConfigEntity(new Long(0), "<MY_API_KEY>", new Integer(4239222), Boolean.FALSE, "random"));
        final List<HipChatConfigEntity> configurations = hipChatRepository.findAll();
        configurations.add(entity);
        for (final HipChatConfigEntity configEntity : configurations) {
            sendHipChatMessage(HIP_CHAT_API, configEntity.getApiKey(), configEntity.getRoomId(), notificationMessage, card, HIP_CHAT_FROM_NAME, configEntity.getNotify(), configEntity.getColor());
        }
    }

    public void sendHipChatMessage(final String apiUrl, final String authToken, final Integer roomId, final String message, final JsonObject card, final String from, final boolean notify, final String color) {
        // TODO find a better way to inject this
        final RestConnection connection = ChannelRestConnectionFactory.createUnauthenticatedRestConnection(apiUrl);
        if (connection != null) {
            final String jsonString = getJsonString(message, card, from, notify, color);
            final RequestBody body = connection.createJsonRequestBody(jsonString);

            final List<String> urlSegments = Arrays.asList("v2", "room", roomId.toString(), "notification");
            final HttpUrl httpUrl = connection.createHttpUrl(urlSegments);

            final Map<String, String> map = new HashMap<>();
            map.put("Authorization", "Bearer " + authToken);
            map.put("Content-Type", "application/json");

            final Request request = connection.createPostRequest(httpUrl, map, body);
            try {
                logger.info("Attempting to send a HipChat message...");
                final Response response = connection.handleExecuteClientCall(request);
                logger.info("Successfully sent a HipChat message!");
                if (logger.isTraceEnabled()) {
                    logger.trace("Response: " + response.toString());
                }
            } catch (final IntegrationException e) {
                logger.error("Failed to send a HipChat message", e);
            }
        }
    }

    public JsonObject formatNotificationEntity(final NotificationEntity entity) {
        final StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Type: ");
        descriptionBuilder.append(entity.getNotificationType());
        descriptionBuilder.append("\nRule: ");
        descriptionBuilder.append(entity.getPolicyRuleName());
        descriptionBuilder.append("\nComponent Name: ");
        descriptionBuilder.append(entity.getComponentName());
        descriptionBuilder.append("\nComponent Version: ");
        descriptionBuilder.append(entity.getComponentVersion());

        final JsonObject card = new JsonObject();
        final JsonObject description = new JsonObject();

        description.addProperty("value", descriptionBuilder.toString());
        description.addProperty("format", "text");
        card.add("description", description);
        card.addProperty("style", "media");
        card.addProperty("format", "medium");
        card.addProperty("title", entity.getProjectName() + " > " + entity.getProjectVersion());
        card.addProperty("id", entity.getId().toString());

        return card;
    }

    private String getJsonString(final String message, final JsonObject card, final String from, final boolean notify, final String color) {
        final JsonObject json = new JsonObject();
        json.addProperty("message", message);
        if (card != null) {
            json.add("card", card);
        }
        json.addProperty("from", from);
        json.addProperty("notify", notify);
        json.addProperty("color", color);

        return json.toString();
    }
}
