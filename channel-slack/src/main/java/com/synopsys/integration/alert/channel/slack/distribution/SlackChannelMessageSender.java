/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.api.channel.ChannelMessageSender;
import com.synopsys.integration.alert.api.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.api.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;

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
        String channelName = slackJobDetails.getChannelName();
        String channelUsername = Optional.ofNullable(slackJobDetails.getChannelUsername())
                                     .orElse(SLACK_DEFAULT_USERNAME);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        IntHttpClient intHttpClient = connectionFactory.createIntHttpClient(webhook);
        RestChannelUtility restChannelUtility = new RestChannelUtility(intHttpClient);

        List<Request> requests = channelMessages.stream()
                                     .map(channelMessage -> createJsonString(channelMessage.getMarkdownContent(), channelName, channelUsername))
                                     .map(jsonString -> restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonString))
                                     .collect(Collectors.toList());

        restChannelUtility.sendMessage(requests, slackChannelKey.getUniversalKey());

        return new MessageResult(String.format("Successfully sent %d Slack message(s)", requests.size()));
    }

    private String createJsonString(String markdownMessage, String channel, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("text", markdownMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

}
