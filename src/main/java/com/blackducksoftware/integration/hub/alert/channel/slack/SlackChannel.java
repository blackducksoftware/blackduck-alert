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
package com.blackducksoftware.integration.hub.alert.channel.slack;

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
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.SlackRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class SlackChannel extends DistributionChannel<SlackEvent, SlackConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(SlackChannel.class);
    private final SlackRepository slackRepository;

    @Autowired
    public SlackChannel(final Gson gson, final SlackRepository slackRepository) {
        super(gson, SlackEvent.class);
        this.slackRepository = slackRepository;
    }

    @Override
    public void sendMessage(final SlackEvent event, final SlackConfigEntity config) {
        final ProjectData projectData = event.getProjectData();
        final String htmlMessage = createHtmlMessage(projectData);
        sendMessage(htmlMessage, config);
    }

    @Override
    public String testMessage(final SlackConfigEntity config) {
        final String message = "Test from Alert application";
        return String.valueOf(sendMessage(message, config));
    }

    private int sendMessage(final String htmlMessage, final SlackConfigEntity config) {
        final String slackUrl = config.getWebhook();
        final RestConnection connection = ChannelRestConnectionFactory.createUnauthenticatedRestConnection(slackUrl);
        if (connection != null) {
            final String jsonString = getJsonString(htmlMessage, config.getChannelName(), config.getUsername());
            final RequestBody body = connection.createJsonRequestBody(jsonString);

            final Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Content-Type", "application/json");

            final HttpUrl httpUrl = connection.createHttpUrl(slackUrl);
            final Request request = connection.createPostRequest(httpUrl, requestProperties, body);

            try {
                logger.info("Attempting to send message...");
                final Response response = connection.handleExecuteClientCall(request);
                logger.info("Successfully sent a message!");
                if (logger.isTraceEnabled()) {
                    logger.trace("Response: " + response.toString());
                }
                return response.code();
            } catch (final IntegrationException e) {
                logger.error("Failed to send message", e);
                return 400;
            }
        } else {
            logger.warn("No message will be sent because a connection was not established.");
        }
        return 500;
    }

    private String getJsonString(final String htmlMessage, final String channel, final String username) {
        final JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);

        return json.toString();
    }

    @JmsListener(destination = SupportedChannels.SLACK)
    @Override
    public void receiveMessage(final String message) {
        logger.info("Received hipchat event message: {}", message);
        final SlackEvent event = getEvent(message);
        logger.info("HipChat event {}", event);

        handleEvent(event);
    }

    public void handleEvent(final SlackEvent event) {
        final List<SlackConfigEntity> configurations = slackRepository.findAll();
        for (final SlackConfigEntity configEntity : configurations) {
            sendMessage(event, configEntity);
        }
    }

}
