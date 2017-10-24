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
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
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
        final String notificationMessage = event.getProjectData().toString();
        final JsonObject card = formatProjectData(event.getProjectData());

        final List<HipChatConfigEntity> configurations = hipChatRepository.findAll();
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

    public JsonObject formatProjectData(final ProjectData projectData) {
        final StringBuilder descriptionBuilder = new StringBuilder();

        final Map<NotificationCategoryEnum, CategoryData> categoryMap = projectData.getCategoryMap();
        if (categoryMap != null) {
            for (final NotificationCategoryEnum category : NotificationCategoryEnum.values()) {
                final CategoryData data = categoryMap.get(category);
                if (data != null) {
                    descriptionBuilder.append("Type: " + data.getCategoryKey());
                    descriptionBuilder.append("\nNumber of Changes: " + data.getItemCount());
                    descriptionBuilder.append("\nItem List: " + data.getItemList().toString());
                    descriptionBuilder.append("\n");
                }
            }
        } else {
            descriptionBuilder.append("A notification was received, but it was empty.");
        }

        final JsonObject card = new JsonObject();
        final JsonObject description = new JsonObject();

        description.addProperty("value", descriptionBuilder.toString());
        description.addProperty("format", "text");
        card.add("description", description);
        card.addProperty("style", "media");
        card.addProperty("format", "medium");
        card.addProperty("title", projectData.getProjectName() + " > " + projectData.getProjectVersion());
        card.addProperty("id", String.valueOf(projectData.hashCode()));

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
