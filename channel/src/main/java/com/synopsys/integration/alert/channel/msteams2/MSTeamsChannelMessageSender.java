/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.api.ChannelMessageSender;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.rest.request.Request;

@Component
public class MSTeamsChannelMessageSender implements ChannelMessageSender<MSTeamsJobDetailsModel, MSTeamsChannelMessageModel, MessageResult> {
    private static final String MESSAGE_THEME_COLOR = "5A2A82"; // Synopsys Purple
    private static final String MESSAGE_SUMMARY = "New Content from Alert";

    private final RestChannelUtility restChannelUtility;
    private final MsTeamsKey msTeamsKey;

    @Autowired
    public MSTeamsChannelMessageSender(RestChannelUtility restChannelUtility, MsTeamsKey msTeamsKey) {
        this.restChannelUtility = restChannelUtility;
        this.msTeamsKey = msTeamsKey;
    }

    @Override
    public MessageResult sendMessages(MSTeamsJobDetailsModel msTeamsJobDetailsModel, List<MSTeamsChannelMessageModel> channelMessages) throws AlertException {
        String webhook = msTeamsJobDetailsModel.getWebhook();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        String provider = createProviderString(channelMessages);
        String messageTitle = String.format("Received a message from %s", provider);
        Request request = createRequestsForMessage(webhook, messageTitle, channelMessages, requestHeaders);

        restChannelUtility.sendSingleMessage(request, msTeamsKey.getUniversalKey());

        return new MessageResult(String.format("Successfully sent %d MSTeams message(s)", channelMessages.size()));
    }

    private String createProviderString(List<MSTeamsChannelMessageModel> channelMessages) {
        return channelMessages.stream()
                   .findFirst()
                   .map(MSTeamsChannelMessageModel::getProviderDetails)
                   .map(ProviderDetails::getProvider)
                   .map(LinkableItem::getValue)
                   .orElse("BlackDuck");
    }

    private Request createRequestsForMessage(String webhook, String messageTitle, List<MSTeamsChannelMessageModel> messages, Map<String, String> requestHeaders) {
        String jsonString = createJsonString(messageTitle, messages);
        return restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonString);
    }

    private String createJsonString(String title, List<MSTeamsChannelMessageModel> messageSections) {
        JsonObject json = new JsonObject();
        json.addProperty("@type", "MessageCard");
        json.addProperty("@context", "https://schema.org/extensions");
        json.addProperty("summary", MESSAGE_SUMMARY);
        json.addProperty("themeColor", MESSAGE_THEME_COLOR);
        json.addProperty("title", title);

        JsonArray jsonArray = new JsonArray();
        for (MSTeamsChannelMessageModel messageSection : messageSections) {
            JsonObject sectionJson = new JsonObject();
            sectionJson.addProperty("startGroup", true);
            sectionJson.addProperty("title", messageSection.getMessageTitle());
            sectionJson.addProperty("text", messageSection.getMessageContent());
            jsonArray.add(sectionJson);
        }
        json.add("sections", jsonArray);

        return json.toString();
    }
}
