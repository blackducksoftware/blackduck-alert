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
package com.synopsys.integration.alert.channel.slack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component(value = SlackChannel.COMPONENT_NAME)
public class SlackChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_slack";
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private static final int MRKDWN_MAX_SIZE_PRE_SPLIT = 3500;
    private static final char SLACK_LINE_SEPARATOR = '\n';
    private static final Map<String, String> SLACK_CHARACTER_ENCODING_MAP;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();
        SLACK_CHARACTER_ENCODING_MAP.put("&", "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put("<", "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put(">", "&gt;");
    }

    private final RestChannelUtility restChannelUtility;

    @Autowired
    public SlackChannel(final Gson gson, final AlertProperties alertProperties, final DefaultAuditUtility auditUtility, final RestChannelUtility restChannelUtility) {
        super(COMPONENT_NAME, gson, alertProperties, auditUtility);
        this.restChannelUtility = restChannelUtility;
    }

    @Override
    public void sendMessage(final DistributionEvent event) throws IntegrationException {
        final List<Request> requests = createRequests(event);
        restChannelUtility.sendMessage(requests, event.getDestination());
    }

    public List<Request> createRequests(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fields = event.getFieldAccessor();
        final String webhook = fields.getString(SlackDescriptor.KEY_WEBHOOK).orElseThrow(() -> new AlertException("Missing Webhook URL"));
        final String channelName = fields.getString(SlackDescriptor.KEY_CHANNEL_NAME).orElseThrow(() -> new AlertException("Missing channel name"));
        final Optional<String> channelUsername = fields.getString(SlackDescriptor.KEY_CHANNEL_USERNAME);
        if (StringUtils.isBlank(event.getContent().getValue())) {
            return List.of();
        } else {
            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");

            final String actualChannelUsername = channelUsername.orElse(SLACK_DEFAULT_USERNAME);
            final List<String> mrkdwnMessagePieces = createMrkdwnMessagePieces(event.getContent());
            return createRequestsForMessage(channelName, actualChannelUsername, webhook, mrkdwnMessagePieces, requestHeaders);
        }
    }

    private List<String> createMrkdwnMessagePieces(final AggregateMessageContent messageContent) {
        final LinkedList<String> messagePieces = new LinkedList<>();

        final StringBuilder topicBuilder = new StringBuilder();
        topicBuilder.append(createLinkableItemString(messageContent, true));
        topicBuilder.append(SLACK_LINE_SEPARATOR);
        final Set<LinkableItem> subTopics = messageContent.getSubTopics();
        if (!subTopics.isEmpty()) {
            final String subTopicName = subTopics
                                            .stream()
                                            .findAny()
                                            .map(LinkableItem::getName)
                                            .orElse(StringUtils.EMPTY);
            appendFormattedItems(topicBuilder, subTopicName, subTopics);
            topicBuilder.append(SLACK_LINE_SEPARATOR);
        }
        topicBuilder.append("- - - - - - - - - - - - - - - - - - - -");
        topicBuilder.append(SLACK_LINE_SEPARATOR);
        messagePieces.add(topicBuilder.toString());

        final SortedSet<CategoryItem> categoryItems = messageContent.getCategoryItems();
        for (final CategoryItem categoryItem : categoryItems) {
            final StringBuilder categoryItemBuilder = new StringBuilder();
            categoryItemBuilder.append("Type: ");
            categoryItemBuilder.append(categoryItem.getOperation());
            categoryItemBuilder.append(SLACK_LINE_SEPARATOR);

            final Map<String, List<LinkableItem>> itemsOfSameName = categoryItem.getItemsOfSameName();
            for (final Map.Entry<String, List<LinkableItem>> namedItems : itemsOfSameName.entrySet()) {
                appendFormattedItems(categoryItemBuilder, namedItems.getKey(), namedItems.getValue());
                categoryItemBuilder.append(SLACK_LINE_SEPARATOR);
            }
            categoryItemBuilder.append(SLACK_LINE_SEPARATOR);
            categoryItemBuilder.append(SLACK_LINE_SEPARATOR);
            messagePieces.add(categoryItemBuilder.toString());
        }

        if (!messagePieces.isEmpty()) {
            final String lastString = messagePieces.removeLast();
            final String modifiedLastString = lastString + SLACK_LINE_SEPARATOR;
            messagePieces.addLast(modifiedLastString);
        }
        return messagePieces;
    }

    private void appendFormattedItems(final StringBuilder mrkdwnBuilder, final String name, final Collection<LinkableItem> namedItemsList) {
        if (namedItemsList.size() == 1) {
            final LinkableItem namedLinkableItem = namedItemsList
                                                       .stream()
                                                       .findFirst()
                                                       .orElseThrow();
            mrkdwnBuilder.append(createLinkableItemString(namedLinkableItem, false));
        } else {
            final String encodedName = createSlackString(name);
            mrkdwnBuilder.append(encodedName);
            mrkdwnBuilder.append(": ");
            for (final LinkableItem item : namedItemsList) {
                final String value = createSlackString(item.getValue());
                final Optional<String> optionalUrl = item.getUrl();

                mrkdwnBuilder.append('[');
                if (optionalUrl.isPresent()) {
                    final String url = createSlackString(optionalUrl.get());
                    mrkdwnBuilder.append('<');
                    mrkdwnBuilder.append(url);
                    mrkdwnBuilder.append('|');
                    mrkdwnBuilder.append(value);
                    mrkdwnBuilder.append('>');
                } else {
                    mrkdwnBuilder.append(value);
                }
                mrkdwnBuilder.append(']');
            }
            mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
        }
    }

    private String createLinkableItemString(final LinkableItem linkableItem, final boolean bold) {
        String format = "%s: <%s|%s>";
        if (bold) {
            format = String.format("*%s*", format);
        }

        final String name = createSlackString(linkableItem.getName());
        final String value = createSlackString(linkableItem.getValue());
        final Optional<String> optionalUrl = linkableItem.getUrl();
        if (optionalUrl.isPresent()) {
            return String.format(format, name, optionalUrl.get(), value);
        }
        if (bold) {
            return String.format("*%s: %s*", name, value);
        }
        return String.format("%s: %s", name, value);
    }

    private String createSlackString(final String unencodedString) {
        String newString = unencodedString;
        for (final Map.Entry<String, String> mapping : SLACK_CHARACTER_ENCODING_MAP.entrySet()) {
            newString = newString.replace(mapping.getKey(), mapping.getValue());
        }
        return newString;
    }

    private List<Request> createRequestsForMessage(final String channelName, final String channelUsername, final String webhook, final List<String> mrkdwnMessagePieces, final Map<String, String> requestHeaders) {
        final List<String> mrkdwnMessageChunks = splitMessages(mrkdwnMessagePieces);
        return mrkdwnMessageChunks
                   .stream()
                   .map(message -> getJsonString(message, channelName, channelUsername))
                   .map(jsonMessage -> restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonMessage))
                   .collect(Collectors.toList());
    }

    private String getJsonString(final String htmlMessage, final String channel, final String username) {
        final JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

    private List<String> splitMessages(final List<String> messagePieces) {
        final List<String> messageChunks = new ArrayList<>();

        StringBuilder chunkBuilder = new StringBuilder();
        for (final String messagePiece : messagePieces) {
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
        return messageChunks;
    }

    private StringBuilder flushChunks(final List<String> messageChunks, final StringBuilder chunkBuilder) {
        messageChunks.add(chunkBuilder.toString());
        return new StringBuilder();
    }

    private List<String> splitMessage(final String message) {
        if (message.length() <= MRKDWN_MAX_SIZE_PRE_SPLIT) {
            return List.of(message);
        }

        final int splitIndex = getSplitIndex(message);
        final String preSplit = message.substring(0, splitIndex);
        final String postSplit = message.substring(splitIndex);

        final List<String> messages = new ArrayList<>();
        messages.add(preSplit);
        messages.addAll(splitMessage(postSplit));

        return messages;
    }

    private int getSplitIndex(final String message) {
        final char bracket = '[';
        final int initialSplitIndex = MRKDWN_MAX_SIZE_PRE_SPLIT - 1;

        final String preSplit = message.substring(0, initialSplitIndex);
        final String postSplit = message.substring(initialSplitIndex);

        final int bracketIndexBefore = preSplit.lastIndexOf(bracket);
        final int newLineIndexBefore = preSplit.lastIndexOf(SLACK_LINE_SEPARATOR);
        final int closestBeforeSplitIndex = Math.max(bracketIndexBefore, newLineIndexBefore);

        final int bracketIndexAfter = postSplit.indexOf(bracket);
        final int newLineIndexAfter = postSplit.indexOf(SLACK_LINE_SEPARATOR);
        final int closestAfterSplitIndex = initialSplitIndex + Math.max(bracketIndexAfter, newLineIndexAfter);

        final int beforeDistance = initialSplitIndex - Math.abs(closestBeforeSplitIndex);
        final int afterDistance = Math.abs(closestAfterSplitIndex) - initialSplitIndex;

        final int closestToSplitIndex;
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
