/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.ChannelMessageSender;
import com.blackduck.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.blackduck.integration.alert.api.channel.rest.RestChannelUtility;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.request.Request;
import com.google.gson.JsonObject;

@Component
public class SlackChannelMessageSender implements ChannelMessageSender<SlackJobDetailsModel, SlackChannelMessageModel, MessageResult> {
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private final SlackChannelKey slackChannelKey;
    private final ChannelRestConnectionFactory connectionFactory;

    @Autowired
    public SlackChannelMessageSender(SlackChannelKey slackChannelKey, ChannelRestConnectionFactory connectionFactory) {
        this.slackChannelKey = slackChannelKey;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public MessageResult sendMessages(SlackJobDetailsModel slackJobDetails, List<SlackChannelMessageModel> channelMessages) throws AlertException {
        String webhook = slackJobDetails.getWebhook();
        String channelUsername = Optional.ofNullable(slackJobDetails.getChannelUsername())
                                     .orElse(SLACK_DEFAULT_USERNAME);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        IntHttpClient intHttpClient = connectionFactory.createIntHttpClient(webhook);
        RestChannelUtility restChannelUtility = new RestChannelUtility(intHttpClient);

        List<Request> requests = channelMessages.stream()
            .map(channelMessage -> createJsonString(channelMessage.getMarkdownContent(), channelUsername))
                                     .map(jsonString -> restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonString))
                                     .collect(Collectors.toList());

        restChannelUtility.sendMessage(requests, slackChannelKey.getUniversalKey());

        return new MessageResult(String.format("Successfully sent %d Slack message(s)", requests.size()));
    }

    private String createJsonString(String markdownMessage, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("text", markdownMessage);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

}
