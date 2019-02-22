/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.slack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.rest.RestDistributionChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.AuditEntryUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

@Component(value = SlackChannel.COMPONENT_NAME)
public class SlackChannel extends RestDistributionChannel {
    public static final String COMPONENT_NAME = "channel_slack";
    public static final String SLACK_API = "https://hooks.slack.com";
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private static final String SLACK_LINE_SEPARATOR = "\n";
    private static final Map<String, String> SLACK_CHARACTER_ENCODING_MAP;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();
        SLACK_CHARACTER_ENCODING_MAP.put("&", "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put("<", "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put(">", "&gt;");
    }

    @Autowired
    public SlackChannel(final Gson gson, final AlertProperties alertProperties, final AuditEntryUtility auditUtility,
        final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(COMPONENT_NAME, gson, alertProperties, auditUtility, channelRestConnectionFactory);
    }

    @Override
    public String getApiUrl(final DistributionEvent event) {
        return SLACK_API;
    }

    @Override
    public List<Request> createRequests(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fields = event.getFieldAccessor();
        final String webhook = fields.getString(SlackDescriptor.KEY_WEBHOOK).orElseThrow(() -> new AlertException("Missing Webhook URL"));
        final String channelName = fields.getString(SlackDescriptor.KEY_CHANNEL_NAME).orElseThrow(() -> new AlertException("Missing channel name"));
        final Optional<String> channelUsername = fields.getString(SlackDescriptor.KEY_CHANNEL_USERNAME);
        if (StringUtils.isBlank(event.getContent().getValue())) {
            return Collections.emptyList();
        } else {
            final String mrkdwnMessage = createMrkdwnMessage(event.getContent());
            final String jsonString = getJsonString(mrkdwnMessage, channelName, channelUsername.orElse(SLACK_DEFAULT_USERNAME));

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");
            return Arrays.asList(createPostMessageRequest(webhook, requestHeaders, jsonString));

        }
    }

    private String createMrkdwnMessage(final AggregateMessageContent messageContent) {
        final StringBuilder mrkdwnBuilder = new StringBuilder();

        mrkdwnBuilder.append(createLinkableItemString(messageContent, true));
        mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
        final Optional<LinkableItem> subTopic = messageContent.getSubTopic();
        if (subTopic.isPresent()) {
            mrkdwnBuilder.append(createLinkableItemString(subTopic.get(), true));
            mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
        }
        mrkdwnBuilder.append("- - - - - - - - - - - - - - - - - - - -");
        mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);

        final List<CategoryItem> categoryItems = messageContent.getCategoryItemList();
        for (final CategoryItem categoryItem : categoryItems) {
            mrkdwnBuilder.append("Type: ");
            mrkdwnBuilder.append(categoryItem.getOperation());
            mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);

            final Map<String, List<LinkableItem>> itemsOfSameName = categoryItem.getItemsOfSameName();
            for (final Map.Entry<String, List<LinkableItem>> namedItems : itemsOfSameName.entrySet()) {
                appendFormattedItems(mrkdwnBuilder, namedItems.getKey(), namedItems.getValue());
                mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
            }
            mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
        }
        mrkdwnBuilder.append(SLACK_LINE_SEPARATOR);
        return mrkdwnBuilder.toString();
    }

    private void appendFormattedItems(final StringBuilder mrkdwnBuilder, final String name, final List<LinkableItem> namedItemsList) {
        if (namedItemsList.size() == 1) {
            final LinkableItem namedLinkableItem = namedItemsList.get(0);
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

    private String getJsonString(final String htmlMessage, final String channel, final String username) {
        final JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }
}
