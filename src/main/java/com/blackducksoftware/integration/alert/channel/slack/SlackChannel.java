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
package com.blackducksoftware.integration.alert.channel.slack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.request.Request;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component(value = SlackChannel.COMPONENT_NAME)
@Transactional
public class SlackChannel extends RestDistributionChannel<GlobalChannelConfigEntity, SlackDistributionConfigEntity> {
    public static final String COMPONENT_NAME = "channel_slack";
    public static final String SLACK_API = "https://hooks.slack.com";

    @Autowired
    public SlackChannel(final Gson gson, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final SlackDistributionRepository slackDistributionRepository,
            final CommonDistributionRepository commonDistributionRepository,
            final ChannelRestConnectionFactory channelRestConnectionFactory, final ContentConverter contentExtractor) {
        super(gson, blackDuckProperties, auditEntryRepository, null, slackDistributionRepository, commonDistributionRepository, channelRestConnectionFactory, contentExtractor);
    }

    @Override
    public String getApiUrl(final GlobalChannelConfigEntity globalConfig) {
        return SLACK_API;
    }

    @Override
    public List<Request> createRequests(final ChannelRequestHelper channelRequestHelper, final SlackDistributionConfigEntity config, final GlobalChannelConfigEntity globalConfig, final ChannelEvent event) throws IntegrationException {
        if (StringUtils.isBlank(config.getWebhook())) {
            throw new IntegrationException("Missing Webhook URL");
        } else if (StringUtils.isBlank(config.getChannelName())) {
            throw new IntegrationException("Missing channel name");
        } else {

            final String slackUrl = config.getWebhook();
            final String htmlMessage = createHtmlMessage(event);
            final String jsonString = getJsonString(htmlMessage, config.getChannelName(), config.getChannelUsername());

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");
            return Arrays.asList(channelRequestHelper.createPostMessageRequest(slackUrl, requestHeaders, jsonString));
        }
    }

    private String createHtmlMessage(final ChannelEvent event) {
        final StringBuilder messageBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(event.getContent())) {
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append(event.getProvider());
            messageBuilder.append(" > ");
            messageBuilder.append(event.getNotificationType());
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append("- - - - - - - - - - - - - - - - - - - -");
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append(event.getContent());
        } else {
            messageBuilder.append(" A notification was received, but it was empty.");
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

}
