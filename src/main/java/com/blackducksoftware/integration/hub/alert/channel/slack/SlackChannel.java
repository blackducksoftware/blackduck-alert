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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackDistributionRepository;
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
public class SlackChannel extends DistributionChannel<SlackEvent, GlobalSlackConfigEntity, SlackDistributionConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(SlackChannel.class);

    SlackDistributionRepository slackRepository;

    @Autowired
    public SlackChannel(final Gson gson, final SlackDistributionRepository slackDistributionRepository, final CommonDistributionRepository commonDistributionRepository) {
        super(gson, null, slackDistributionRepository, commonDistributionRepository, SlackEvent.class);
        this.slackRepository = slackDistributionRepository;
    }

    @Override
    public void sendMessage(final SlackEvent event, final SlackDistributionConfigEntity config) {
        final ProjectData projectData = event.getProjectData();
        final String htmlMessage = createMessage(projectData);
        try {
            sendMessage(htmlMessage, config);
        } catch (final IntegrationException e) {
            if (e instanceof IntegrationRestException) {
                logger.error(((IntegrationRestException) e).getHttpStatusCode() + ":" + ((IntegrationRestException) e).getHttpStatusMessage());
            }
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String testMessage(final SlackDistributionConfigEntity distributionConfig) throws IntegrationException {
        final String message = "*Test* from _Alert_ application";
        return sendMessage(message, distributionConfig);
    }

    private String sendMessage(final String htmlMessage, final SlackDistributionConfigEntity config) throws IntegrationException {
        final String slackUrl = config.getWebhook();
        final ChannelRestConnectionFactory restConnectionFactory = new ChannelRestConnectionFactory(null);
        final RestConnection connection = restConnectionFactory.createUnauthenticatedRestConnection(slackUrl);
        if (connection != null) {
            final String jsonString = getJsonString(htmlMessage, config.getChannelName(), config.getChannelUsername());
            final RequestBody body = connection.createJsonRequestBody(jsonString);

            final Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Content-Type", "application/json");

            final HttpUrl httpUrl = connection.createHttpUrl(slackUrl);
            final Request request = connection.createPostRequest(httpUrl, requestProperties, body);

            try {
                logger.info("Attempting to send message...");
                final Response response = connection.handleExecuteClientCall(request);
                logger.info("Successfully sent a slack message!");
                if (logger.isTraceEnabled()) {
                    logger.trace("Response: " + response.toString());
                }
                return "Attempting to send message";
            } catch (final IntegrationException e) {
                throw new IntegrationRestException(400, "Failed to send Slack message", e.getMessage(), e);
            }
        } else {
            throw new IntegrationRestException(500, "No message will be sent because a connection was not established.", "No message will be sent because a connection was not established.");
        }
    }

    protected String createMessage(final ProjectData projectData) {
        final StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("*");
        messageBuilder.append(projectData.getProjectName());
        messageBuilder.append(" ");
        messageBuilder.append(projectData.getProjectVersion());
        messageBuilder.append("*");
        messageBuilder.append(System.lineSeparator());

        final Map<NotificationCategoryEnum, CategoryData> categoryMap = projectData.getCategoryMap();
        if (categoryMap != null) {
            for (final NotificationCategoryEnum category : NotificationCategoryEnum.values()) {
                final CategoryData data = categoryMap.get(category);
                if (data != null) {
                    messageBuilder.append("Type: ");
                    messageBuilder.append(data.getCategoryKey());
                    messageBuilder.append(System.lineSeparator());
                    messageBuilder.append("Number of Changes: ");
                    messageBuilder.append(data.getItemCount());
                    for (final ItemData item : data.getItemList()) {
                        messageBuilder.append(System.lineSeparator());
                        final Map<String, Object> dataSet = item.getDataSet();
                        messageBuilder.append("Rule: _" + dataSet.get(ItemTypeEnum.RULE.toString()));
                        messageBuilder.append("_" + System.lineSeparator());
                        messageBuilder.append("Component: _" + dataSet.get(ItemTypeEnum.COMPONENT.toString()));
                        messageBuilder.append("_ [" + dataSet.get(ItemTypeEnum.VERSION.toString()) + "]");
                    }
                }
            }
        } else {
            messageBuilder.append("_A notification was received, but it was empty._");
        }
        return messageBuilder.toString();
    }

    private String getJsonString(final String htmlMessage, final String channel, final String username) {
        final JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

    @JmsListener(destination = SupportedChannels.SLACK)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

}
