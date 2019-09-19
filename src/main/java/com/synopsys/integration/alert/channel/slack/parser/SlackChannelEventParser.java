/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.slack.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class SlackChannelEventParser {
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private static final int MRKDWN_MAX_SIZE_PRE_SPLIT = 3500;

    private final SlackChannelMessageParser slackChannelMessageParser;
    private final RestChannelUtility restChannelUtility;

    @Autowired
    public SlackChannelEventParser(SlackChannelMessageParser slackChannelMessageParser, RestChannelUtility restChannelUtility) {
        this.slackChannelMessageParser = slackChannelMessageParser;
        this.restChannelUtility = restChannelUtility;
    }

    public List<Request> createRequests(DistributionEvent event) throws IntegrationException {
        FieldAccessor fields = event.getFieldAccessor();

        String webhook = fields.getString(SlackDescriptor.KEY_WEBHOOK).orElse("");
        String channelName = fields.getString(SlackDescriptor.KEY_CHANNEL_NAME).orElse("");

        Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isBlank(webhook)) {
            fieldErrors.put(SlackDescriptor.KEY_WEBHOOK, "Missing Webhook URL");
        }
        if (StringUtils.isBlank(channelName)) {
            fieldErrors.put(SlackDescriptor.KEY_CHANNEL_NAME, "Missing channel name");
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }

        String channelUsername = fields.getString(SlackDescriptor.KEY_CHANNEL_USERNAME).orElse(SLACK_DEFAULT_USERNAME);
        MessageContentGroup eventContent = event.getContent();
        if (!eventContent.isEmpty()) {
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");

            List<String> mrkdwnMessagePieces = slackChannelMessageParser.createMessagePieces(eventContent);
            return createRequestsForMessage(channelName, channelUsername, webhook, mrkdwnMessagePieces, requestHeaders);
        }
        return List.of();
    }

    private List<Request> createRequestsForMessage(String channelName, String channelUsername, String webhook, List<String> mrkdwnMessagePieces, Map<String, String> requestHeaders) {
        List<String> mrkdwnMessageChunks = splitMessages(mrkdwnMessagePieces);
        return mrkdwnMessageChunks
                   .stream()
                   .filter(StringUtils::isNotBlank)
                   .map(message -> getJsonString(message, channelName, channelUsername))
                   .map(jsonMessage -> restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonMessage))
                   .collect(Collectors.toList());
    }

    private String getJsonString(String htmlMessage, String channel, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

    private List<String> splitMessages(List<String> messagePieces) {
        List<String> messageChunks = new ArrayList<>();

        StringBuilder chunkBuilder = new StringBuilder();
        for (String messagePiece : messagePieces) {
            if (messagePiece.length() <= MRKDWN_MAX_SIZE_PRE_SPLIT) {
                if (messagePiece.length() + chunkBuilder.length() <= MRKDWN_MAX_SIZE_PRE_SPLIT) {
                    chunkBuilder.append(messagePiece);
                } else {
                    chunkBuilder = flushChunks(messageChunks, chunkBuilder);
                    messageChunks.add(messagePiece);
                }
            } else {
                chunkBuilder = flushChunks(messageChunks, chunkBuilder);
                messageChunks.addAll(splitMessage(messagePiece));
            }
        }

        if (chunkBuilder.length() > 0) {
            flushChunks(messageChunks, chunkBuilder);
        }
        return messageChunks;
    }

    private StringBuilder flushChunks(List<String> messageChunks, StringBuilder chunkBuilder) {
        messageChunks.add(chunkBuilder.toString());
        return new StringBuilder();
    }

    private List<String> splitMessage(String message) {
        if (message.length() <= MRKDWN_MAX_SIZE_PRE_SPLIT) {
            return List.of(message);
        }

        int splitIndex = getSplitIndex(message);
        String preSplit = message.substring(0, splitIndex);
        String postSplit = message.substring(splitIndex);

        List<String> messages = new ArrayList<>();
        messages.add(preSplit);
        messages.addAll(splitMessage(postSplit));

        return messages;
    }

    private int getSplitIndex(String message) {
        char bracket = '[';
        int initialSplitIndex = MRKDWN_MAX_SIZE_PRE_SPLIT - 1;

        String preSplit = message.substring(0, initialSplitIndex);
        String postSplit = message.substring(initialSplitIndex);

        int bracketIndexBefore = preSplit.lastIndexOf(bracket);
        int newLineIndexBefore = preSplit.lastIndexOf(slackChannelMessageParser.getLineSeparator());
        int closestBeforeSplitIndex = Math.max(bracketIndexBefore, newLineIndexBefore);

        int bracketIndexAfter = postSplit.indexOf(bracket);
        int newLineIndexAfter = postSplit.indexOf(slackChannelMessageParser.getLineSeparator());
        int closestAfterSplitIndex = initialSplitIndex + Math.max(bracketIndexAfter, newLineIndexAfter);

        int beforeDistance = initialSplitIndex - Math.abs(closestBeforeSplitIndex);
        int afterDistance = Math.abs(closestAfterSplitIndex) - initialSplitIndex;

        int closestToSplitIndex;
        if (beforeDistance < afterDistance) {
            closestToSplitIndex = closestBeforeSplitIndex;
        } else {
            closestToSplitIndex = closestAfterSplitIndex;
        }

        if (closestToSplitIndex != -1) {
            return closestToSplitIndex;
        }

        return message.length() - 1;
    }

}
