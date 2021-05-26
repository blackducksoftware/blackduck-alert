/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.api.ChannelMessageSender;
import com.synopsys.integration.alert.channel.api.rest.RestChannelUtility;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
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

        List<Request> messageRequests = channelMessages.stream()
                                            .map(it -> createRequestForMessage(webhook, it, requestHeaders))
                                            .collect(Collectors.toList());

        restChannelUtility.sendMessage(messageRequests, msTeamsKey.getUniversalKey());

        return new MessageResult(String.format("Successfully sent %d MSTeams message(s)", channelMessages.size()));
    }

    private Request createRequestForMessage(String webhook, MSTeamsChannelMessageModel messageModel, Map<String, String> requestHeaders) {
        String jsonString = createJsonString(messageModel);
        return restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonString);
    }

    private String createJsonString(MSTeamsChannelMessageModel messageModel) {
        JsonObject json = new JsonObject();
        json.addProperty("@type", "MessageCard");
        json.addProperty("@context", "https://schema.org/extensions");
        json.addProperty("summary", MESSAGE_SUMMARY);
        json.addProperty("themeColor", MESSAGE_THEME_COLOR);
        json.addProperty("title", messageModel.getTitle());

        JsonArray jsonArray = new JsonArray();
        for (MSTeamsChannelMessageSection messageSection : messageModel.getSections()) {
            JsonObject sectionJson = new JsonObject();
            sectionJson.addProperty("startGroup", true);
            sectionJson.addProperty("title", messageSection.getTitle());
            sectionJson.addProperty("text", messageSection.getContent());
            jsonArray.add(sectionJson);
        }
        json.add("sections", jsonArray);

        return json.toString();
    }

}
