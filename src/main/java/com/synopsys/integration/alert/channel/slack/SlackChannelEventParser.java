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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component
public class SlackChannelEventParser {
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
    public SlackChannelEventParser(RestChannelUtility restChannelUtility) {
        this.restChannelUtility = restChannelUtility;
    }

    public List<Request> createRequests(DistributionEvent event) throws IntegrationException {
        FieldAccessor fields = event.getFieldAccessor();

        String webhook = fields.getString(SlackDescriptor.KEY_WEBHOOK).orElse("");
        String channelName = fields.getString(SlackDescriptor.KEY_CHANNEL_NAME).orElse("");

        if (StringUtils.isBlank(webhook) || StringUtils.isBlank(channelName)) {
            Map<String, String> fieldErrors = new HashMap<>();
            if (StringUtils.isBlank(webhook)) {
                fieldErrors.put(SlackDescriptor.KEY_WEBHOOK, "Missing Webhook URL");
            }
            if (StringUtils.isBlank(channelName)) {
                fieldErrors.put(SlackDescriptor.KEY_CHANNEL_NAME, "Missing channel name");
            }
            throw new AlertFieldException(fieldErrors);
        } else {
            Optional<String> channelUsername = fields.getString(SlackDescriptor.KEY_CHANNEL_USERNAME);

            MessageContentGroup eventContent = event.getContent();
            if (eventContent.isEmpty()) {
                return List.of();
            } else {
                Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("Content-Type", "application/json");

                String actualChannelUsername = channelUsername.orElse(SLACK_DEFAULT_USERNAME);
                List<String> mrkdwnMessagePieces = createMrkdwnMessagePieces(eventContent);
                return createRequestsForMessage(channelName, actualChannelUsername, webhook, mrkdwnMessagePieces, requestHeaders);
            }
        }
    }

    private List<String> createMrkdwnMessagePieces(MessageContentGroup messageContentGroup) {
        LinkedList<String> messagePieces = new LinkedList<>();

        StringBuilder topicBuilder = new StringBuilder();
        topicBuilder.append(createLinkableItemString(messageContentGroup.getCommonTopic(), true));
        topicBuilder.append(SLACK_LINE_SEPARATOR);
        messagePieces.add(topicBuilder.toString());

        for (ProviderMessageContent messageContent : messageContentGroup.getSubContent()) {
            StringBuilder subTopicBuilder = new StringBuilder();
            messageContent
                .getSubTopic()
                .map(subTopic -> createLinkableItemString(subTopic, true))
                .ifPresent(subTopicBuilder::append);
            subTopicBuilder.append(SLACK_LINE_SEPARATOR);
            subTopicBuilder.append("- - - - - - - - - - - - - - - - - - - -");
            subTopicBuilder.append(SLACK_LINE_SEPARATOR);
            messagePieces.add(subTopicBuilder.toString());

            SetMap<String, ComponentItem> componentItemSetMap = messageContent.groupRelatedComponentItems();
            for (Set<ComponentItem> similarItems : componentItemSetMap.values()) {
                String componentItemMrkdwn = createComponentItemMrkdwn(similarItems);
                messagePieces.add(componentItemMrkdwn);
            }

            if (!messagePieces.isEmpty()) {
                String lastString = messagePieces.removeLast();
                String modifiedLastString = lastString + SLACK_LINE_SEPARATOR;
                messagePieces.addLast(modifiedLastString);
            }
        }
        return messagePieces;
    }

    // TODO Clean this up. Maybe create a helper for this logic that can be utilized elsewhere.
    private String createComponentItemMrkdwn(Set<ComponentItem> componentItems) {
        StringBuilder componentItemBuilder = new StringBuilder();
        boolean collapseOnCategory = componentItems
                                         .stream()
                                         .allMatch(ComponentItem::collapseOnCategory);
        if (collapseOnCategory) {
            Optional<ComponentItem> optionalArbitraryItem = componentItems
                                                                .stream()
                                                                .findAny();
            if (optionalArbitraryItem.isPresent()) {
                ComponentItem arbitraryItem = optionalArbitraryItem.get();
                componentItemBuilder.append(createCommonComponentItemString(arbitraryItem));
                componentItemBuilder.append(SLACK_LINE_SEPARATOR);
                componentItemBuilder.append(createSlackString(arbitraryItem.getCategoryItem().getName()));
                componentItemBuilder.append(": ");
            }
            Set<LinkableItem> categoryItems = componentItems
                                                  .stream()
                                                  .map(ComponentItem::getCategoryItem)
                                                  .collect(Collectors.toSet());
            for (LinkableItem categoryItem : categoryItems) {
                String linkableItemValueString = createLinkableItemValueString(categoryItem);
                componentItemBuilder.append("[");
                componentItemBuilder.append(linkableItemValueString);
                componentItemBuilder.append("]");
            }
            componentItemBuilder.append(SLACK_LINE_SEPARATOR);

            // FIXME need to include component attributes
        } else {
            for (ComponentItem componentItem : componentItems) {
                componentItemBuilder.append(createCommonComponentItemString(componentItem));
                componentItemBuilder.append(SLACK_LINE_SEPARATOR);
                componentItemBuilder.append(createLinkableItemString(componentItem.getCategoryItem(), false));
                componentItem
                    .getSubCategoryItem()
                    .map(subCategoryItem -> createLinkableItemString(subCategoryItem, false))
                    .ifPresent(linkableItemString -> {
                        componentItemBuilder.append(linkableItemString);
                        componentItemBuilder.append(SLACK_LINE_SEPARATOR);
                    });

                Set<LinkableItem> componentAttributes = componentItem.getComponentAttributes();
                for (LinkableItem attribute : componentAttributes) {
                    componentItemBuilder.append(createLinkableItemString(attribute, false));
                    componentItemBuilder.append(SLACK_LINE_SEPARATOR);
                }
                componentItemBuilder.append(SLACK_LINE_SEPARATOR);
                componentItemBuilder.append(SLACK_LINE_SEPARATOR);
            }
        }
        return componentItemBuilder.toString();
    }

    private String createCommonComponentItemString(ComponentItem componentItem) {
        StringBuilder componentItemBuilder = new StringBuilder();
        componentItemBuilder.append("Category: ");
        componentItemBuilder.append(componentItem.getCategory());
        componentItemBuilder.append(SLACK_LINE_SEPARATOR);
        componentItemBuilder.append("Operation: ");
        componentItemBuilder.append(componentItem.getOperation());
        componentItemBuilder.append(SLACK_LINE_SEPARATOR);
        componentItemBuilder.append(createLinkableItemString(componentItem.getComponent(), false));
        componentItemBuilder.append(SLACK_LINE_SEPARATOR);
        componentItem
            .getSubComponent()
            .map(subComponent -> createLinkableItemString(subComponent, false))
            .ifPresent(linkableItemString -> {
                componentItemBuilder.append(linkableItemString);
                componentItemBuilder.append(SLACK_LINE_SEPARATOR);
            });
        return componentItemBuilder.toString();
    }

    private String createLinkableItemString(LinkableItem linkableItem, boolean bold) {
        String name = createSlackString(linkableItem.getName());
        String value = createSlackString(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString;
        if (optionalUrl.isPresent()) {
            String linkableItemValueString = createLinkableItemValueString(linkableItem);
            formattedString = String.format("%s: %s", name, linkableItemValueString);
        } else {
            formattedString = String.format("%s: %s", name, value);
        }

        if (bold) {
            return String.format("*%s*", formattedString);
        }
        return formattedString;
    }

    private String createLinkableItemValueString(LinkableItem linkableItem) {
        String value = createSlackString(linkableItem.getValue());
        Optional<String> optionalUrl = linkableItem.getUrl();

        String formattedString = value;
        if (optionalUrl.isPresent()) {
            String urlString = createSlackString(optionalUrl.get());
            formattedString = String.format("<%s|%s>", urlString, value);
        }
        return formattedString;
    }

    private String createSlackString(String unencodedString) {
        String newString = unencodedString;
        for (Map.Entry<String, String> mapping : SLACK_CHARACTER_ENCODING_MAP.entrySet()) {
            newString = newString.replace(mapping.getKey(), mapping.getValue());
        }
        return newString;
    }

    private List<Request> createRequestsForMessage(String channelName, String channelUsername, String webhook, List<String> mrkdwnMessagePieces, Map<String, String> requestHeaders) {
        List<String> mrkdwnMessageChunks = splitMessages(mrkdwnMessagePieces);
        return mrkdwnMessageChunks
                   .stream()
                   .filter(message -> StringUtils.isNotBlank(message))
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
        int newLineIndexBefore = preSplit.lastIndexOf(SLACK_LINE_SEPARATOR);
        int closestBeforeSplitIndex = Math.max(bracketIndexBefore, newLineIndexBefore);

        int bracketIndexAfter = postSplit.indexOf(bracket);
        int newLineIndexAfter = postSplit.indexOf(SLACK_LINE_SEPARATOR);
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
